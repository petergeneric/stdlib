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
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.concurrent.TimeUnit;

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
		// Register the exception processor
		resteasyProviderFactory.registerProviderInstance(remoteExceptionClientResponseFilter);

		// Set up the Connection Manager
		this.connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);

		manager.register(this);
	}


	/**
	 * Retrieve a single shared ResteasyClient for non-fast fail connections
	 *
	 * @return
	 */
	public ResteasyClient getClient()
	{
		if (client == null || client.isClosed())
			client = newClient(false, null, null);

		return client;
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
	 *
	 * @return
	 */
	public ResteasyClient newClient(final boolean fastFail, AuthScope authScope, Credentials credentials)
	{
		// If credentials were supplied then we should set them up
		CredentialsProvider credentialsProvider;
		if (credentials != null)
		{
			credentialsProvider = new BasicCredentialsProvider();

			if (authScope != null)
				credentialsProvider.setCredentials(authScope, credentials);
			else
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
		}
		else
		{
			credentialsProvider = null;
		}

		// Build an HttpClient instance
		final CloseableHttpClient http = createHttpClient(fastFail, credentialsProvider);

		// Build a RestEasy client
		return new ResteasyClientBuilder().httpEngine(new ApacheHttpClient4Engine(http))
		                                  .providerFactory(resteasyProviderFactory)
		                                  .build();
	}


	private CloseableHttpClient createHttpClient(boolean fastFailTimeouts, final CredentialsProvider provider)
	{
		RequestConfig.Builder requestBuilder = RequestConfig.custom();

		if (fastFailTimeouts)
		{
			requestBuilder.setConnectTimeout((int) fastFailConnectionTimeout.getMilliseconds())
			              .setSocketTimeout((int) fastFailSocketTimeout.getMilliseconds());
		}
		else
		{
			requestBuilder.setConnectTimeout((int) connectionTimeout.getMilliseconds())
			              .setSocketTimeout((int) socketTimeout.getMilliseconds());
		}

		HttpClientBuilder builder = HttpClientBuilder.create();

		builder.setConnectionManager(connectionManager);

		if (provider != null)
			builder.setDefaultCredentialsProvider(provider);

		builder.setDefaultRequestConfig(requestBuilder.build());

		// Prohibit keepalive if desired
		if (noKeepalive)
			builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());

		return builder.build();
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();

		if (client != null)
			client.close();
	}
}
