package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
	private final PoolingClientConnectionManager connectionManager;
	private final ResteasyProviderFactory resteasyProviderFactory;

	@Inject(optional = true)
	@Named("jaxrs.connection.timeout")
	Timeout connectionTimeout = new Timeout(20, TimeUnit.SECONDS);

	@Inject(optional = true)
	@Named("jaxrs.socket.timeout")
	Timeout socketTimeout = new Timeout(5, TimeUnit.MINUTES);

	@Inject(optional = true)
	@Named("jaxrs.nokeepalive")
	boolean noKeepalive = true;

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


		this.connectionManager = new PoolingClientConnectionManager();

		manager.register(this);
	}


	/**
	 * Retrieve a single shared ResteasyClient
	 *
	 * @return
	 */
	public ResteasyClient getClient()
	{
		if (client == null || client.isClosed())
			client = newClient(null, null);

		return client;
	}


	/**
	 * Build a new Resteasy Client, optionally with authentication credentials
	 *
	 * @param authScope
	 * 		the auth scope to use - if null then defaults to <code>AuthScope.ANY</code>
	 * @param credentials
	 * 		the credentials to use (optional, e.g. {@link org.apache.http.auth.UsernamePasswordCredentials})
	 *
	 * @return
	 */
	public ResteasyClient newClient(AuthScope authScope, Credentials credentials)
	{
		final DefaultHttpClient http = createHttpClient();

		// If credentials were supplied then we should set them up
		if (credentials != null)
		{
			if (authScope != null)
				http.getCredentialsProvider().setCredentials(authScope, credentials);
			else
				http.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
		}

		return new ResteasyClientBuilder().httpEngine(new ApacheHttpClient4Engine(http))
		                                  .providerFactory(resteasyProviderFactory)
		                                  .build();
	}


	private DefaultHttpClient createHttpClient()
	{
		DefaultHttpClient client = new DefaultHttpClient(connectionManager);

		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, (int) connectionTimeout.getMilliseconds());
		HttpConnectionParams.setSoTimeout(params, (int) socketTimeout.getMilliseconds());

		// Prohibit keepalive if desired
		if (noKeepalive)
			client.setReuseStrategy(new NoConnectionReuseStrategy());

		return client;
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();

		if (client != null)
			client.close();
	}
}
