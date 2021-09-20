package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.net.ProxySelector;
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
	                                 final TracingClientRequestFilter tracingRequestFilter,
	                                 final RemoteExceptionClientResponseFilter remoteExceptionClientResponseFilter,
	                                 final JAXBContextResolver jaxbContextResolver)
	{
		// Make sure that if we're called multiple times (e.g. because of a guice CreationException failing startup after us) we start fresh
		if (ResteasyProviderFactory.peekInstance() != null)
			ResteasyProviderFactory.clearInstanceIfEqual(ResteasyProviderFactory.peekInstance());

		this.resteasyProviderFactory = ResteasyProviderFactory.getInstance();
		resteasyProviderFactory.registerProviderInstance(jaxbContextResolver);

		// Register the joda param converters
		resteasyProviderFactory.registerProviderInstance(new CommonTypesParamConverterProvider());

		// Register the exception processor
		if (remoteExceptionClientResponseFilter != null)
			resteasyProviderFactory.registerProviderInstance(remoteExceptionClientResponseFilter);

		// Register provider that always tries to get fastinfoset rather than application/xml if the fast infoset plugin is available
		FastInfosetPreferringClientRequestFilter.register();

		if (tracingRequestFilter != null)
			resteasyProviderFactory.registerProviderInstance(tracingRequestFilter);

		// Set up the Connection Manager
		this.connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);

		if (manager != null)
			manager.register(this);
	}


	public ResteasyClient getOrCreateClient(final NativeHttpClientBuilder.AuthCredential credentials,
	                                         final boolean fastFail,
	                                         final boolean storeCookies)
	{
		final boolean useShared = (!fastFail && !storeCookies && credentials == null);

		if (useShared && this.client != null)
			return client;

		// Now build a resteasy client
		{
			ResteasyClientBuilder builder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();

			// TODO how to register this?
			//builder.providerFactory(resteasyProviderFactory);

			if (storeCookies)
				builder.enableCookieManagement();

			if (fastFail)
			{
				builder.connectTimeout(this.fastFailConnectionTimeout.getMilliseconds(), TimeUnit.MILLISECONDS);
				builder.readTimeout(fastFailSocketTimeout.getMilliseconds(), TimeUnit.MILLISECONDS);
			}
			else
			{
				builder.connectTimeout(this.connectionTimeout.getMilliseconds(), TimeUnit.MILLISECONDS);
				builder.readTimeout(socketTimeout.getMilliseconds(), TimeUnit.MILLISECONDS);
			}

			// Build and apply the HttpEngine
			{
				final var engineBuilder = new NativeHttpClientBuilder().resteasyClientBuilder(builder);

				if (credentials != null)
					engineBuilder.withAuth(credentials);

				builder.httpEngine(engineBuilder.build());
			}

			if (useShared)
			{
				this.client = builder.build();
				return this.client;
			}
			else
			{
				return builder.build();
			}
		}
	}


	/**
	 * Build an HttpClient
	 *
	 * @param customiser
	 * @return
	 */
	public CloseableHttpClient createHttpClient(final Consumer<HttpClientBuilder> customiser)
	{
		final HttpClientBuilder builder = HttpClientBuilder.create();

		// By default set long call timeouts
		{
			RequestConfig.Builder requestBuilder = RequestConfig.custom();

			requestBuilder
					.setConnectTimeout((int) connectionTimeout.getMilliseconds())
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

		// Allow customisation
		if (customiser != null)
			customiser.accept(builder);

		return builder.build();
	}


	/**
	 * Combine two consumers. Consumers may be null, in which case this selects the non-null one. If both are null then will
	 * return <code>null</code>
	 *
	 * @param a   the first consumer (optional)
	 * @param b   the second consumer (optional)
	 * @param <T>
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
