package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of a JAX-RS Dynamic Proxy HTTP Client Factory that uses RestEasy
 */
@Singleton
public class ResteasyClientFactoryImpl implements JAXRSProxyClientFactory, StoppableService
{
	private final PoolingClientConnectionManager connectionManager;
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


	@Inject
	public ResteasyClientFactoryImpl(final ShutdownManager manager,
	                                 final ResteasyClientErrorInterceptor errorInterceptor,
	                                 final JAXBContextResolver jaxbContextResolver)
	{
		this.resteasyProviderFactory = ResteasyProviderFactory.getInstance();
		resteasyProviderFactory.addClientErrorInterceptor(errorInterceptor);
		resteasyProviderFactory.registerProviderInstance(jaxbContextResolver);

		// Register the joda param converters
		resteasyProviderFactory.registerProviderInstance(new CommonTypesParamConverterProvider());

		this.connectionManager = new PoolingClientConnectionManager();

		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);

		manager.register(this);
	}


	@Override
	public <T> T createClient(final Class<T> iface, final String endpoint)
	{
		return createClient(iface, URI.create(endpoint));
	}


	@Override
	public <T> T createClient(Class<T> iface, URI endpoint)
	{
		final boolean fastFailTimeouts = iface.isAnnotationPresent(FastFailServiceClient.class);

		final ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(createClient(fastFailTimeouts));

		return ProxyFactory.create(iface, endpoint, executor, resteasyProviderFactory);
	}


	private DefaultHttpClient createClient(boolean fastFailTimeouts)
	{
		DefaultHttpClient client = new DefaultHttpClient(connectionManager);

		HttpParams params = client.getParams();

		if (fastFailTimeouts)
		{
			HttpConnectionParams.setConnectionTimeout(params, (int) fastFailConnectionTimeout.getMilliseconds());
			HttpConnectionParams.setSoTimeout(params, (int) fastFailSocketTimeout.getMilliseconds());
		}
		else
		{
			HttpConnectionParams.setConnectionTimeout(params, (int) connectionTimeout.getMilliseconds());
			HttpConnectionParams.setSoTimeout(params, (int) socketTimeout.getMilliseconds());
		}

		// Prohibit keepalive if desired
		if (noKeepalive)
		{
			client.setReuseStrategy(new NoConnectionReuseStrategy());
		}

		return client;
	}


	@Override
	public <T> T createClientWithPasswordAuth(Class<T> iface, URI endpoint, String username, String password)
	{
		final boolean fastFailTimeouts = iface.isAnnotationPresent(FastFailServiceClient.class);

		final DefaultHttpClient client = createClient(fastFailTimeouts);

		final Credentials credentials = new UsernamePasswordCredentials(username, password);

		client.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

		final ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
		return ProxyFactory.create(iface, endpoint, executor, resteasyProviderFactory);
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();
	}
}
