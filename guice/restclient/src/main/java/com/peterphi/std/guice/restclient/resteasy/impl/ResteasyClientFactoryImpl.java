package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.threading.Timeout;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
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
	private final ThreadSafeClientConnManager connectionManager;
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


	@Inject
	public ResteasyClientFactoryImpl(final ShutdownManager manager,
	                                 final ResteasyClientErrorInterceptor errorInterceptor,
	                                 final JAXBContextResolver jaxbContextResolver)
	{
		this.resteasyProviderFactory = ResteasyProviderFactory.getInstance();
		resteasyProviderFactory.addClientErrorInterceptor(errorInterceptor);
		resteasyProviderFactory.registerProviderInstance(jaxbContextResolver);

		this.connectionManager = new ThreadSafeClientConnManager();

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
		final ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(createClient());

		return ProxyFactory.create(iface, endpoint, executor, resteasyProviderFactory);
	}


	private DefaultHttpClient createClient()
	{
		DefaultHttpClient client = new DefaultHttpClient(connectionManager);

		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, (int) connectionTimeout.getMilliseconds());
		HttpConnectionParams.setSoTimeout(params, (int) socketTimeout.getMilliseconds());


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
		final DefaultHttpClient client = createClient();

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
