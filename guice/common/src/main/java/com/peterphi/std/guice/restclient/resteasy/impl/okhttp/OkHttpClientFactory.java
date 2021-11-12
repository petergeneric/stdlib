package com.peterphi.std.guice.restclient.resteasy.impl.okhttp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.NotImplementedException;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.resteasy.impl.HttpClientFactory;
import com.peterphi.std.threading.Timeout;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpClientFactory implements HttpClientFactory, StoppableService
{
	private static class ClientFeatures
	{
		public final boolean h2c;
		public final boolean fastFail;
		public final boolean cookies;


		public ClientFeatures(final boolean h2c, final boolean fastFail, final boolean cookies)
		{
			this.h2c = h2c;
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

			if (h2c != that.h2c)
				return false;
			if (fastFail != that.fastFail)
				return false;
			return cookies == that.cookies;
		}


		@Override
		public int hashCode()
		{
			int result = (h2c ? 1 : 0);
			result = 31 * result + (fastFail ? 1 : 0);
			result = 31 * result + (cookies ? 1 : 0);
			return result;
		}


		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder("ClientFeatures{");
			sb.append("h2c=").append(h2c);
			sb.append(", fastFail=").append(fastFail);
			sb.append(", cookies=").append(cookies);
			sb.append('}');
			return sb.toString();
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

	private final Map<ClientFeatures, OkHttpClientEngine> clients = new HashMap<>();


	@Inject
	public OkHttpClientFactory(ShutdownManager shutdownManager)
	{
		if (shutdownManager != null)
			shutdownManager.register(this);
	}


	@Override
	public synchronized OkHttpClientEngine getClient(final boolean h2c, final boolean fastFail, final boolean cookies)
	{
		ClientFeatures key = new ClientFeatures(h2c, fastFail, cookies);
		return clients.computeIfAbsent(key, this :: createClient);
	}


	@Override
	public boolean willVaryWithH2C()
	{
		return true;
	}


	private OkHttpClientEngine createClient(final ClientFeatures key)
	{
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		if (key.cookies)
			throw new NotImplementedException("OkHttp Cookie Jar not supported!");

		if (key.h2c)
			builder.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE));

		if (key.fastFail)
		{
			builder.readTimeout(Duration.ofMillis(this.fastFailSocketTimeout.getMilliseconds()));
			builder.connectTimeout(Duration.ofMillis(this.fastFailConnectionTimeout.getMilliseconds()));
		}
		else
		{
			builder.readTimeout(Duration.ofMillis(this.socketTimeout.getMilliseconds()));
			builder.connectTimeout(Duration.ofMillis(this.connectionTimeout.getMilliseconds()));
		}

		return new OkHttpClientEngine(builder.build());
	}


	@Override
	public synchronized void shutdown()
	{
		for (OkHttpClientEngine engine : clients.values())
		{
			engine.client.connectionPool().evictAll();
		}

		clients.clear();
	}
}
