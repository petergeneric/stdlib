package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.logging.LoggingMDCConstants;
import com.peterphi.std.guice.common.logging.logreport.jaxrs.LogReportMessageBodyWriter;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Builds ResteasyClient objects
 */
@Singleton
public class ResteasyClientFactoryImpl implements StoppableService
{
	private final PoolingHttpClientConnectionManager connectionManager;
	private final ResteasyProviderFactory resteasyProviderFactory;


	@Inject(optional = true)
	@Named("jaxrs.connection.timeout")
	@Doc("The connection timeout for HTTP sockets (default 20s)")
	Timeout connectionTimeout = new Timeout(20, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.socket.timeout")
	@Doc("The Socket Timeout for HTTP sockets (default 5m)")
	Timeout socketTimeout = new Timeout(5, TimeUnit.MINUTES);


	@Inject(optional = true)
	@Named("jaxrs.fast-fail.connection.timeout")
	@Doc("The connection timeout for HTTP sockets created for Fast Fail clients (default 15s)")
	Timeout fastFailConnectionTimeout = new Timeout(15, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.fast-fail.socket.timeout")
	@Doc("The Socket Timeout for HTTP sockets created for Fast Fail clients (default 15s)")
	Timeout fastFailSocketTimeout = new Timeout(15, TimeUnit.SECONDS);


	@Inject(optional = true)
	@Named("jaxrs.nokeepalive")
	@Doc("If true, keepalive will be disabled for HTTP connections (default true)")
	boolean noKeepalive = true;

	@Inject(optional = true)
	@Named("jaxrs.max-connections-per-route")
	@Doc("The maximum number of connections per HTTP route (default MAXINT)")
	int maxConnectionsPerRoute = Integer.MAX_VALUE;

	@Inject(optional = true)
	@Named("jaxrs.max-total-connections")
	@Doc("The maximum number of HTTP connections in total across all routes (default MAXINT)")
	int maxConnectionsTotal = Integer.MAX_VALUE;

	private ResteasyClient client;


	@Inject
	public ResteasyClientFactoryImpl(final ShutdownManager manager,
	                                 final RemoteExceptionClientResponseFilter remoteExceptionClientResponseFilter,
	                                 final JAXBContextResolver jaxbContextResolver)
	{
		this.resteasyProviderFactory = ResteasyProviderFactory.getInstance();
		resteasyProviderFactory.registerProviderInstance(jaxbContextResolver);

		// Register the joda param converters
		resteasyProviderFactory.registerProviderInstance(new CommonTypesParamConverterProvider());

		// Register the LogReport reader
		// TODO find a better way to handle registration for external applications?
		resteasyProviderFactory.registerProviderInstance(new LogReportMessageBodyWriter());

		// Register the exception processor
		if (remoteExceptionClientResponseFilter != null)
			resteasyProviderFactory.registerProviderInstance(remoteExceptionClientResponseFilter);

		// Set up the Connection Manager
		this.connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);

		if (manager != null)
			manager.register(this);
	}


	/**
	 * Build a new Resteasy Client, optionally with authentication credentials
	 *
	 * @param fastFail
	 * 		if true, use fast fail timeouts, otherwise false to use default timeouts
	 * @param authScope
	 * 		the auth scope to use - if null then defaults to <code>AuthScope.ANY</code>
	 * @param credentials
	 * 		the credentials to use (optional, e.g. {@link org.apache.http.auth.UsernamePasswordCredentials})
	 * @param customiser
	 * 		optional HttpClientBuilder customiser.
	 *
	 * @return
	 */
	public ResteasyClient getOrCreateClient(final boolean fastFail,
	                                        final AuthScope authScope,
	                                        final Credentials credentials,
	                                        final boolean preemptiveAuth,
	                                        final boolean storeCookies,
	                                        Consumer<HttpClientBuilder> customiser)
	{
		customiser = createHttpClientCustomiser(fastFail, authScope, credentials, preemptiveAuth, storeCookies, customiser);


		return getOrCreateClient(customiser, null);
	}


	/**
	 * N.B. This method signature may change in the future to add new parameters
	 *
	 * @param fastFail
	 * @param authScope
	 * @param credentials
	 * @param preemptiveAuth
	 * @param storeCookies
	 * @param customiser
	 *
	 * @return
	 */
	public Consumer<HttpClientBuilder> createHttpClientCustomiser(final boolean fastFail,
	                                                              final AuthScope authScope,
	                                                              final Credentials credentials,
	                                                              final boolean preemptiveAuth,
	                                                              final boolean storeCookies,
	                                                              Consumer<HttpClientBuilder> customiser)
	{
		// Customise timeouts if fast fail mode is enabled
		if (fastFail)
		{
			customiser = concat(customiser, b ->
			{
				RequestConfig.Builder requestBuilder = RequestConfig.custom();

				requestBuilder.setConnectTimeout((int) fastFailConnectionTimeout.getMilliseconds())
				              .setSocketTimeout((int) fastFailSocketTimeout.getMilliseconds());

				b.setDefaultRequestConfig(requestBuilder.build());
			});
		}

		// If credentials were supplied then we should set them up
		if (credentials != null)
		{
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

			if (authScope != null)
				credentialsProvider.setCredentials(authScope, credentials);
			else
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);

			// Set up bearer auth scheme provider if we're using bearer credentials
			if (credentials instanceof BearerCredentials)
			{
				customiser = concat(customiser, b ->
				{
					Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().register(
							"Bearer",
							new BearerAuthSchemeProvider()).build();
					b.setDefaultAuthSchemeRegistry(authSchemeRegistry);
				});
			}

			// Set up the credentials customisation
			customiser = concat(customiser, b -> b.setDefaultCredentialsProvider(credentialsProvider));

			if (preemptiveAuth && credentials instanceof BearerCredentials)
				customiser = concat(customiser, b -> b.addInterceptorFirst(new PreemptiveBearerAuthInterceptor()));
			else
				customiser = concat(customiser, b -> b.addInterceptorLast(new PreemptiveBasicAuthInterceptor()));
		}

		// If cookies are enabled then set up a cookie store
		if (storeCookies)
			customiser = concat(customiser, b -> b.setDefaultCookieStore(new BasicCookieStore()));

		return customiser;
	}


	private ResteasyClient getOrCreateClient(Consumer<HttpClientBuilder> httpCustomiser,
	                                         Consumer<ResteasyClientBuilder> resteasyCustomiser)
	{
		if (httpCustomiser == null && resteasyCustomiser == null)
		{
			// Recursively call self to create a shared client for other non-customised consumers
			if (client == null)
				client = getOrCreateClient(Objects:: requireNonNull, Objects:: requireNonNull);

			return client; // use shared client
		}
		else
		{
			final CloseableHttpClient http = createHttpClient(httpCustomiser);


			// Now build a resteasy client
			{
				ResteasyClientBuilder builder = new ResteasyClientBuilder();

				builder.httpEngine(new ApacheHttpClient4Engine(http)).providerFactory(resteasyProviderFactory);

				if (resteasyCustomiser != null)
					resteasyCustomiser.accept(builder);

				return builder.build();
			}
		}
	}


	/**
	 * Build an HttpClient
	 *
	 * @param customiser
	 *
	 * @return
	 */
	public CloseableHttpClient createHttpClient(final Consumer<HttpClientBuilder> customiser)
	{
		final HttpClientBuilder builder = HttpClientBuilder.create();

		// By default set long call timeouts
		{
			RequestConfig.Builder requestBuilder = RequestConfig.custom();

			requestBuilder.setConnectTimeout((int) connectionTimeout.getMilliseconds())
			              .setSocketTimeout((int) socketTimeout.getMilliseconds());

			builder.setDefaultRequestConfig(requestBuilder.build());
		}

		// Set the default keepalive setting
		if (noKeepalive)
			builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());

		// By default share the common connection provider
		builder.setConnectionManager(connectionManager);

		// By default use the JRE default route planner for proxies
		builder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));

		// If a correlation id is set locally then make sure we pass it along to the remote service
		// N.B. we use the value from the MDC because the correlation id could be for a internal task
		builder.addInterceptorFirst(new HttpRequestInterceptor()
		{
			@Override
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
			{
				final String traceId = MDC.get(LoggingMDCConstants.TRACE_ID);

				if (traceId != null)
					request.addHeader("X-Correlation-ID", traceId);
			}
		});

		// Allow customisation
		if (customiser != null)
			customiser.accept(builder);

		return builder.build();
	}


	/**
	 * Combine two consumers. Consumers may be null, in which case this selects the non-null one. If both are null then will
	 * return <code>null</code>
	 *
	 * @param a
	 * 		the first consumer (optional)
	 * @param b
	 * 		the second consumer (optional)
	 * @param <T>
	 *
	 * @return
	 */
	private static <T> Consumer<T> concat(Consumer<T> a, Consumer<T> b)
	{
		if (a != null && b != null) // both non-null
			return a.andThen(b);
		else if (a != null)
			return a;
		else
			return b;
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();

		if (client != null)
			client.close();
	}
}
