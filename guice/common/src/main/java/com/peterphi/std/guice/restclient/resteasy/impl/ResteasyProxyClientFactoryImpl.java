package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceServiceProperties;
import com.peterphi.std.guice.common.breaker.BreakerService;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Singleton
public class ResteasyProxyClientFactoryImpl implements JAXRSProxyClientFactory
{
	/**
	 * A Bearer Token generator that generates Delegated Tokens for an OAuth2 user session when calling other services
	 */
	public static final String OAUTH_DELEGATING_BEARER_GENERATOR = "com.peterphi.std.guice.web.rest.auth.oauth2.OAuthSessionDelegatingBearerGenerator";

	@Inject
	ResteasyClientFactoryImpl clientFactory;

	@Inject
	GuiceConfig config;

	@Inject
	BreakerService breakerService;

	@Inject
	Injector guice;

	@Inject(optional = true)
	@Named("jaxrs.cookie-store")
	@Doc("Whether default JAX-RS clients should maintain a cookie store (default false); will also default to false if oauth delegation is switched on (or if a bearer generator is configured)")
	public boolean defaultStoreCookies = false;

	/**
	 * Counter that keeps track of the number of currently-paused service calls within this service
	 */
	private final AtomicInteger pausedCallsCounter = new AtomicInteger(0);


	public ResteasyProxyClientFactoryImpl()
	{
	}


	public ResteasyProxyClientFactoryImpl(ResteasyClientFactoryImpl clientFactory, GuiceConfig config)
	{
		this.clientFactory = clientFactory;
		this.config = config;
	}


	public static String getConfiguredBoundServiceName(final GuiceConfig config, Class<?> iface, String... names)
	{
		if (names == null || names.length == 0)
		{
			if (iface == null)
				throw new IllegalArgumentException("If not specifying service names you must provide a service interface");
			else
				names = getServiceNames(iface);
		}

		for (String name : names)
		{
			if (name == null)
				continue;

			if (config.containsKey(GuiceServiceProperties.prop(GuiceServiceProperties.ENDPOINT, name)))
				return name;
		}

		return null;
	}


	@Override
	public ResteasyWebTarget getWebTarget(final String... names)
	{
		return getWebTarget(false, names);
	}


	@Override
	public ResteasyWebTarget getWebTarget(final Class<?> iface, final String... names)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		return getWebTarget(fastFail, names);
	}

	private ResteasyWebTarget getWebTarget(final boolean defaultFastFail, final String... names)
	{
		final String name = getConfiguredBoundServiceName(config, null, names);

		if (name == null)
			throw new IllegalArgumentException("Cannot find service in configuration by any of these names: " +
			                                   Arrays.asList(names));

		final String endpoint = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.ENDPOINT, name), null);
		final URI uri = URI.create(endpoint);

		// TODO allow other per-service configuration?
		final String username = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.USERNAME, name), getUsername(uri));
		final String password = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.PASSWORD, name), getPassword(uri));
		final boolean fastFail = config.getBoolean(GuiceServiceProperties.prop(GuiceServiceProperties.FAST_FAIL, name), defaultFastFail);
		final String authType = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.AUTH_TYPE, name), GuiceConstants.JAXRS_CLIENT_AUTH_DEFAULT);
		final String bearerToken = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.BEARER_TOKEN, name), null);
		final boolean h2c = uri.getScheme().equalsIgnoreCase("http") && config.getBoolean(GuiceServiceProperties.prop(GuiceServiceProperties.H2C, name), false); // h2c with prior knowledge
		final boolean oauthDelegate = config.getBoolean(GuiceServiceProperties.prop(GuiceServiceProperties.SHOULD_DELEGATE_USER_TOKEN, name), false);
		final String defaultBearerGenerator;

		if (oauthDelegate)
			defaultBearerGenerator = OAUTH_DELEGATING_BEARER_GENERATOR;
		else
			defaultBearerGenerator = null;

		final String bearerTokenClassName = config.get(GuiceServiceProperties.prop(GuiceServiceProperties.BEARER_GENERATOR, name), defaultBearerGenerator);

		// N.B. do not store cookies by default if we're generating bearer tokens (since this may result in credentials being improperly shared across calls)
		final boolean storeCookies = config.getBoolean(GuiceServiceProperties.prop(GuiceServiceProperties.STORE_COOKIES, name),
		                                               defaultStoreCookies && (bearerTokenClassName == null));

		final BearerGenerator bearerSupplier;
		{
			if (bearerTokenClassName != null)
			{
				try
				{
					final Class<? extends BearerGenerator> bearerClass = (Class) Class.forName(bearerTokenClassName);

					bearerSupplier = guice.getInstance(bearerClass);
				}
				catch (Throwable e)
				{
					throw new RuntimeException("Error trying to instantiate bearer-generator class " + bearerTokenClassName, e);
				}
			}
			else if (bearerToken != null)
			{
				// Static bearer token
				bearerSupplier = new StaticBearerToken(bearerToken);
			}
			else
			{
				bearerSupplier = null;
			}
		}

		final boolean preemptiveAuth;
		if (bearerSupplier != null)
			preemptiveAuth = true; // force pre-emptive auth
		else if (authType.equalsIgnoreCase(GuiceConstants.JAXRS_CLIENT_AUTH_DEFAULT))
			preemptiveAuth = false;
		else if (authType.equalsIgnoreCase(GuiceConstants.JAXRS_CLIENT_AUTH_PREEMPT))
			preemptiveAuth = true;
		else
			throw new IllegalArgumentException("Illegal auth-type for service " + name + ": " + authType);

		return createWebTarget(uri, h2c, fastFail, username, password, bearerSupplier, storeCookies, preemptiveAuth);
	}


	@Override
	public <T> T getClient(final Class<T> iface, final String... names)
	{
		return getClient(iface, getWebTarget(iface, names), names);
	}


	@Override
	public <T> T getClient(final Class<T> iface)
	{
		return getClient(iface, getServiceNames(iface));
	}


	@Override
	public <T> T getClient(final Class<T> iface, final WebTarget target)
	{
		return getClient(iface, target, new String[0]);
	}


	private <T> T getClient(final Class<T> iface, final WebTarget target, final String... names)
	{
		final ResteasyWebTarget resteasyTarget = (ResteasyWebTarget) target;
		final T proxy = resteasyTarget.proxy(iface);

		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		// Set up a Pausable Proxy that will allow us to pause service calls by tripping a breaker
		PausableProxy handler = createPausableProxy(proxy, fastFail, names);

		return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, handler);
	}


	@NotNull
	private <T> PausableProxy createPausableProxy(final T proxy, final boolean fastFail, final String[] names)
	{
		List<String> nameList = new ArrayList<>();
		nameList.add("restclient.all");
		for (String name : names)
		{
			nameList.add("restclient." + name);
		}

		PausableProxy handler = new PausableProxy(proxy, fastFail, pausedCallsCounter);
		breakerService.register(handler :: setPaused, nameList);

		return handler;
	}


	/**
	 * Computes the default set of names for a service based on an interface class. The names produced are an ordered list:
	 * <ul>
	 * <li>The fully qualified class name</li>
	 * <li>If present, the {@link com.peterphi.std.annotation.ServiceName} annotation on the class (OR if not specified on the
	 * class, the {@link com.peterphi.std.annotation.ServiceName} specified on the package)</li>
	 * <li>The simple name of the class (the class name without the package prefix)</li>
	 * </ul>
	 *
	 * @param iface a JAX-RS service interface
	 * @return An array containing one or more names that could be used for the class; may contain nulls (which should be ignored)
	 */
	private static String[] getServiceNames(Class<?> iface)
	{
		Objects.requireNonNull(iface, "Missing param: iface!");

		return new String[]{iface.getName(), getServiceName(iface), iface.getSimpleName()};
	}


	private static String getServiceName(Class<?> iface)
	{
		Objects.requireNonNull(iface, "Missing param: iface!");

		if (iface.isAnnotationPresent(ServiceName.class))
		{
			return iface.getAnnotation(ServiceName.class).value();
		}
		else if (iface.getPackage().isAnnotationPresent(ServiceName.class))
		{
			return iface.getPackage().getAnnotation(ServiceName.class).value();
		}
		else
		{
			return null; // No special name
		}
	}


	@Override
	public ResteasyWebTarget createWebTarget(final URI endpoint, String username, String password)
	{
		return createWebTarget(endpoint, username, password, null, defaultStoreCookies, true);
	}


	public ResteasyWebTarget createWebTarget(final URI endpoint,
	                                         String username,
	                                         String password,
	                                         BearerGenerator bearerToken,
	                                         boolean storeCookies,
	                                         boolean preemptiveAuth)
	{
		return createWebTarget(endpoint, false, false, username, password, bearerToken, storeCookies, preemptiveAuth);
	}


	ResteasyWebTarget createWebTarget(URI endpoint,
									  boolean h2c,
	                                  final boolean fastFail,
	                                  final String username,
	                                  final String password,
	                                  final BearerGenerator bearerToken,
	                                  final boolean storeCookies,
	                                  final boolean preemptiveAuth)
	{
		// Allow the use of the "h2c://" scheme as an alias for http:// with h2c=true
		// This allows a broader set of users to set h2c without having to explicitly provide an h2c flag
		if ("h2c".equalsIgnoreCase(endpoint.getScheme()))
		{
			h2c = true;
			endpoint = UriBuilder.fromUri(endpoint).scheme("http").build();
		}

		final ResteasyClientFactoryImpl.AuthScope scope = new ResteasyClientFactoryImpl.AuthScope(endpoint.getScheme(),
		                                                                                    endpoint.getHost(),
		                                                                                    -1);

		final ResteasyClientFactoryImpl.AuthCredential credentials;
		if (bearerToken != null)
			credentials = new ResteasyClientFactoryImpl.BearerTokenCredentials(scope, bearerToken);
		else if (username != null)
			credentials = new ResteasyClientFactoryImpl.UsernamePasswordCredentials(scope, username, password, preemptiveAuth);
		else
			credentials = null;

		return clientFactory.getOrCreateClient(credentials, fastFail, storeCookies, h2c).target(endpoint);
	}


	@Override
	public <T> T createClient(final Class<T> iface, final String endpoint)
	{
		return createClient(iface, URI.create(endpoint));
	}


	@Override
	public <T> T createClient(Class<T> iface, URI endpoint)
	{
		return createClient(iface, endpoint, false);
	}


	@Override
	public <T> T createClient(final Class<T> iface, final URI endpoint, final boolean preemptiveAuth)
	{
		return createClientWithPasswordAuth(iface, endpoint, getUsername(endpoint), getPassword(endpoint), preemptiveAuth);
	}


	@Override
	@Deprecated
	public <T> T createClientWithPasswordAuth(Class<T> iface, URI endpoint, String username, String password)
	{
		return createClientWithPasswordAuth(iface, endpoint, username, password, false);
	}


	@Override
	public <T> T createClientWithBearerAuth(final Class<T> iface, final URI endpoint, final Supplier<String> token)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		return createWebTarget(endpoint,
							   false,
		                       fastFail,
		                       null,
		                       null,
		                       new SupplierBearerGenerator(token),
		                       defaultStoreCookies,
		                       true).proxy(iface);
	}


	@Override
	public <T> T createClientWithPasswordAuth(final Class<T> iface,
	                                          final URI endpoint,
	                                          final String username,
	                                          final String password,
	                                          final boolean preemptiveAuth)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		return createWebTarget(endpoint, false, fastFail, username, password, null, defaultStoreCookies, preemptiveAuth).proxy(iface);
	}


	private static String getUsername(URI endpoint)
	{
		final String info = endpoint.getUserInfo();

		if (StringUtils.isEmpty(info))
			return null;
		else if (info.indexOf(':') != -1)
			return info.split(":", 2)[0];
		else
			return null;
	}


	private static String getPassword(URI endpoint)
	{
		final String info = endpoint.getUserInfo();

		if (StringUtils.isEmpty(info))
			return null;
		else if (info.indexOf(':') != -1)
			return info.split(":", 2)[1];
		else
			return null;
	}
}
