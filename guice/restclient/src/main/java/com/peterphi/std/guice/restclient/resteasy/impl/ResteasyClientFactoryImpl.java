package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Builds ResteasyClient objects
 */
@Singleton
public class ResteasyClientFactoryImpl implements StoppableService
{
	private Logger log = Logger.getLogger(ResteasyClientFactoryImpl.class);
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
	@Named("jaxrs.connection.request.timeout")
	@Doc("The Connection request Timeout for HTTP sockets (default 5m)")
	Timeout connectionRequestTimeout = new Timeout(5, TimeUnit.MINUTES);

	@Inject(optional = true)
	@Named("jaxrs.fast-fail.connection.request.timeout")
	@Doc("The connection request timeout for HTTP sockets created for Fast Fail clients (default 15s)")
	Timeout fastFailConnectionRequestTimeout = new Timeout(15, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.fast-fail.socket.timeout")
	@Doc("The Socket Timeout for HTTP sockets created for Fast Fail clients (default 15s)")
	Timeout fastFailSocketTimeout = new Timeout(15, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.fast-fail.connection.timeout")
	@Doc("The connection timeout for HTTP sockets created for Fast Fail clients (default 15s)")
	Timeout fastFailConnectionTimeout = new Timeout(15, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.nokeepalive")
	@Doc("If true, keepalive will be disabled for HTTP connections (default true)")
	boolean noKeepalive = true;

	@Inject(optional = true)
	@Named("jaxrs.fail-fast.revalidate")
	@Doc("The revalidation interval for a stale connection (default 5 seconds) ")
	Timeout revalidateTime = new Timeout(5, TimeUnit.SECONDS);

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
		// Register the exception processor
		resteasyProviderFactory.registerProviderInstance(remoteExceptionClientResponseFilter);

		// Set up the Connection Manager
		this.connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);
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
	 * 		optional HttpClientBuilder customiser
	 *
	 * @return
	 */
	public ResteasyClient getOrCreateClient(final boolean fastFail,
	                                        final AuthScope authScope,
	                                        final Credentials credentials,
	                                        final boolean preemptiveAuth,
	                                        Consumer<HttpClientBuilder> customiser)
	{
		// Customise timeouts if fast fail mode is enabled
		if (fastFail)
		{
			customiser = concat(customiser, b -> {
				RequestConfig.Builder requestBuilder = RequestConfig.custom();
				requestBuilder.setConnectionRequestTimeout((int)fastFailConnectionRequestTimeout.getMilliseconds());
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

			// Set up the credentials customisation
			customiser = concat(customiser, b -> b.setDefaultCredentialsProvider(credentialsProvider));

			if (preemptiveAuth)
				customiser = concat(customiser, b -> b.addInterceptorLast(new PreemptiveBasicAuthInterceptor()));
		}

		return getOrCreateClient(customiser, null);
	}


	private ResteasyClient getOrCreateClient(Consumer<HttpClientBuilder> httpCustomiser,
	                                         Consumer<ResteasyClientBuilder> resteasyCustomiser)
	{
		RequestConfig.Builder requestBuilder;
		if (httpCustomiser == null && resteasyCustomiser == null)
		{
			// Recursively call self to create a shared client for other non-customised consumers
			if (client == null)
				client = getOrCreateClient(b -> {
					// nothing to customise, supplied so we don't take this code path again
				}, b -> {
					// nothing to customise, supplied so we don't take this code path again
				});

			return client; // use shared client
		}
		else
		{
			// Build an HttpClient
			final CloseableHttpClient http;
			{
				final HttpClientBuilder builder = HttpClientBuilder.create();

				// By default set long call timeouts
				{
					requestBuilder = RequestConfig.custom();

					requestBuilder.setConnectTimeout((int) connectionTimeout.getMilliseconds())
					              .setSocketTimeout((int) socketTimeout.getMilliseconds())
							      .setConnectionRequestTimeout((int)connectionRequestTimeout.getMilliseconds());

					builder.setDefaultRequestConfig(requestBuilder.build());
				}

				// Set the default keepalive setting
				if (noKeepalive)
					builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());

				// By default share the common connection provider
				builder.setConnectionManager(connectionManager);

				// Allow customisation
				if (httpCustomiser != null)
					httpCustomiser.accept(builder);

				http = builder.build();
			}

			// Now build a resteasy client
			{
				ResteasyClientBuilder builder = new ResteasyClientBuilder();

				builder.httpEngine(new ResteasyClientExecutor(http)).providerFactory(resteasyProviderFactory);

				if (resteasyCustomiser != null)
					resteasyCustomiser.accept(builder);

				return builder.build();
			}
		}
	}


	/**
	 * Combine a number of consumers together, ignoring nulls
	 *
	 * @param consumers
	 * @param <T>
	 *
	 * @return
	 */
	private static <T> Consumer<T> concat(Consumer<T>... consumers)
	{
		Consumer<T> rootConsumer = null;

		for (Consumer<T> consumer : consumers)
			if (consumer != null)
			{
				if (rootConsumer == null)
					rootConsumer = consumer;
				else
					rootConsumer = rootConsumer.andThen(consumer);
			}

		return rootConsumer;
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();

		if (client != null)
			client.close();
	}
}
