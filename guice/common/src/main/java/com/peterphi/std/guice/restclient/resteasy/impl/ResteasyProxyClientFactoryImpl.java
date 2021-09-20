package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
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
	Injector guice;

	@Inject(optional = true)
	@Named("jaxrs.cookie-store")
	@Doc("Whether default JAX-RS clients should maintain a cookie store (default false); will also default to false if oauth delegation is switched on (or if a bearer generator is configured)")
	public boolean defaultStoreCookies = false;


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

			if (config.containsKey("service." + name + ".endpoint"))
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

		final String endpoint = config.get("service." + name + ".endpoint", null);
		final URI uri = URI.create(endpoint);

		// TODO allow other per-service configuration?
		final String username = config.get("service." + name + ".username", getUsername(uri));
		final String password = config.get("service." + name + ".password", getPassword(uri));
		final boolean fastFail = config.getBoolean("service." + name + ".fast-fail", defaultFastFail);
		final String authType = config.get("service." + name + ".auth-type", GuiceConstants.JAXRS_CLIENT_AUTH_DEFAULT);
		final String bearerToken = config.get("service." + name + ".bearer", null);
		final boolean oauthDelegate = config.getBoolean("service." + name + ".delegation", false);
		final String defaultBearerGenerator;

		if (oauthDelegate)
			defaultBearerGenerator = OAUTH_DELEGATING_BEARER_GENERATOR;
		else
			defaultBearerGenerator = null;

		final String bearerTokenClassName = config.get("service." + name + ".bearer-generator", defaultBearerGenerator);

		// N.B. do not store cookies by default if we're generating bearer tokens (since this may result in credentials being improperly shared across calls)
		final boolean storeCookies = config.getBoolean("service." + name + ".cookie-store",
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

		return createWebTarget(uri, fastFail, username, password, bearerSupplier, storeCookies, preemptiveAuth);
	}


	@Override
	public <T> T getClient(final Class<T> iface, final String... names)
	{
		return getWebTarget(iface, names).proxy(iface);
	}


	@Override
	public <T> T getClient(final Class<T> iface)
	{
		return getClient(iface, getServiceNames(iface));
	}


	@Override
	public <T> T getClient(final Class<T> iface, final WebTarget target)
	{
		final ResteasyWebTarget resteasyTarget = (ResteasyWebTarget) target;

		return resteasyTarget.proxy(iface);
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
		return createWebTarget(endpoint, false, username, password, bearerToken, storeCookies, preemptiveAuth);
	}


	ResteasyWebTarget createWebTarget(final URI endpoint,
	                                  final boolean fastFail,
	                                  final String username,
	                                  final String password,
	                                  final BearerGenerator bearerToken,
	                                  final boolean storeCookies,
	                                  final boolean preemptiveAuth)
	{
		final NativeHttpClientBuilder.AuthScope scope = new NativeHttpClientBuilder.AuthScope(endpoint.getScheme(),
		                                                                                      endpoint.getHost(),
		                                                                                      -1);

		final NativeHttpClientBuilder.AuthCredential credentials;
		if (bearerToken != null)
			credentials = new NativeHttpClientBuilder.BearerTokenCredentials(scope, bearerToken);
		else if (username != null)
			credentials = new NativeHttpClientBuilder.UsernamePasswordCredentials(scope, username, password, preemptiveAuth);
		else
			credentials = null;

		return clientFactory.getOrCreateClient(credentials, fastFail, storeCookies).target(endpoint);
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

		return createWebTarget(endpoint, fastFail, username, password, null, defaultStoreCookies, preemptiveAuth).proxy(iface);
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
