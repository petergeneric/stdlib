package com.peterphi.std.guice.restclient.resteasy.impl.apache43;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.resteasy.impl.HttpClientFactory;
import com.peterphi.std.threading.Timeout;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClientEngine;

import java.net.ProxySelector;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApacheHttpClientFactory implements HttpClientFactory, StoppableService
{
	private static class ClientFeatures
	{
		public final boolean fastFail;
		public final boolean cookies;


		public ClientFeatures(final boolean fastFail, final boolean cookies)
		{
			this.fastFail = fastFail;
			this.cookies = cookies;
		}


		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (!(o instanceof ClientFeatures))
				return false;

			ClientFeatures that = (ClientFeatures) o;

			if (fastFail != that.fastFail)
				return false;
			return cookies == that.cookies;
		}


		@Override
		public int hashCode()
		{
			int result = (fastFail ? 1 : 0);
			result = 31 * result + (cookies ? 1 : 0);
			return result;
		}
	}

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


	private final Map<ClientFeatures, CloseableHttpClient> clients = new HashMap<>();
	private final PoolingHttpClientConnectionManager connectionManager;


	@Inject
	public ApacheHttpClientFactory(ShutdownManager shutdownManager)
	{
		this.connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
		connectionManager.setMaxTotal(maxConnectionsTotal);

		if (shutdownManager != null)
			shutdownManager.register(this);
	}


	@Override
	public synchronized ClientHttpEngine getClient(final boolean h2c, final boolean fastFail, final boolean cookies)
	{
		ClientFeatures key = new ClientFeatures(fastFail, cookies);
		CloseableHttpClient client = clients.computeIfAbsent(key, this :: createClient);
		if (client == null)
			client = createClient(key);

		return ApacheHttpClientEngine.create(client, false);
	}


	@Override
	public boolean willVaryWithH2C()
	{
		return false; // Does not support H2C with prior knowledge
	}


	private CloseableHttpClient createClient(final ClientFeatures key)
	{
		final HttpClientBuilder builder = HttpClientBuilder.create();

		// By default set long call timeouts
		{
			RequestConfig.Builder requestBuilder = RequestConfig.custom();

			if (key.fastFail)
				requestBuilder
						.setConnectTimeout((int) fastFailConnectionTimeout.getMilliseconds())
						.setSocketTimeout((int) fastFailSocketTimeout.getMilliseconds());
			else
				requestBuilder
						.setConnectTimeout((int) connectionTimeout.getMilliseconds())
						.setSocketTimeout((int) socketTimeout.getMilliseconds());

			builder.setDefaultRequestConfig(requestBuilder.build());
		}

		if (key.cookies)
			builder.setDefaultCookieStore(new BasicCookieStore());

		// Set the default keepalive setting
		if (noKeepalive)
			builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());

		// By default share the common connection provider
		builder.setConnectionManager(connectionManager);

		// By default use the JRE default route planner for proxies
		builder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));

		return builder.build();
	}


	@Override
	public synchronized void shutdown()
	{
		for (CloseableHttpClient client : clients.values())
		{
			IOUtils.closeQuietly(client);
		}

		clients.clear();
	}
}
