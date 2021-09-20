package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import com.peterphi.std.threading.Timeout;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Builds ResteasyClient objects
 */
@Singleton
public class ResteasyClientFactoryImpl implements StoppableService
{
	private static final Logger log = Logger.getLogger(ResteasyClientFactoryImpl.class);

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
	private ResteasyClient clientH2C;

	private OkHttpClient defaultH2CClient;
	private OkHttpClient defaultHttpClient;


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


	public ResteasyClient getOrCreateClient(final AuthCredential credentials,
	                                        final boolean fastFail,
	                                        final boolean storeCookies,
	                                        final boolean h2c)
	{
		// First: get the OkHttp client
		OkHttpClient httpClient = null;
		if (!fastFail && !storeCookies)
		{
			if (h2c)
			{
				if (this.defaultH2CClient == null)
				{
					createDefaultH2CClient();
				}
				httpClient = this.defaultH2CClient;
			}
			else
			{
				if (this.defaultHttpClient == null)
				{
					createDefaultHttpClient();
				}
				httpClient = this.defaultHttpClient;
			}
		}

		final boolean isSharedHttpClient = (httpClient != null);

		// Unauthenticated ResteasyClients can be shared
		if (isSharedHttpClient && credentials == null)
		{
			if (httpClient == defaultH2CClient && this.clientH2C != null)
			{
				return this.clientH2C;
			}
			else if (httpClient == defaultHttpClient && this.client != null)
			{
				return this.client;
			}
		}

		// Now build a resteasy client
		ResteasyClientBuilder builder = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();

		// TODO how to register this?
		//builder.providerFactory(resteasyProviderFactory);

		if (credentials != null)
		{
			builder.register(new ClientRequestFilter()
			{
				@Override
				public void filter(final ClientRequestContext ctx) throws IOException
				{
					final var uri = ctx.getUri();
					if (credentials.scope().test(uri.getScheme(), uri.getHost(), uri.getPort()))
					{
						if (credentials instanceof BearerTokenCredentials token)
						{
							ctx
									.getHeaders()
									.putIfAbsent("Authorization",
									             Collections.singletonList("Bearer " + token.token().getToken()));
						}
						else if (credentials instanceof UsernamePasswordCredentials passwd)
						{
							final String headerVal = Credentials.basic(passwd.username(), passwd.password());

							ctx.getHeaders().putIfAbsent("Authorization", Collections.singletonList(headerVal));
						}
					}
				}
			});
		}

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
		if (isSharedHttpClient)
		{
			// Use the shared HTTP client
			builder.httpEngine(new OkHttpClientEngine(httpClient));
		}
		else
		{
			// Build a new HTTP client just for this engine
			builder.httpEngine(new OkHttpClientBuilder().resteasyClientBuilder(builder).build());
		}


		if (credentials == null && httpClient != null)
		{
			if (httpClient == defaultH2CClient)
			{
				this.clientH2C = builder.build();
				return this.clientH2C;
			}
			else if (httpClient == defaultHttpClient)
			{
				this.client = builder.build();
				return this.client;
			}
		}


		return builder.build();
	}


	private synchronized void createDefaultH2CClient()
	{
		if (this.defaultH2CClient == null)
		{
			this.defaultH2CClient = new OkHttpClient.Builder()
					.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE))
					.readTimeout(Duration.ofMillis(this.socketTimeout.getMilliseconds()))
					.connectTimeout(Duration.ofMillis(this.connectionTimeout.getMilliseconds()))
					.build();
		}
	}


	private synchronized void createDefaultHttpClient()
	{
		if (this.defaultHttpClient == null)
		{
			this.defaultHttpClient = new OkHttpClient.Builder()
					.readTimeout(Duration.ofMillis(this.socketTimeout.getMilliseconds()))
					.connectTimeout(Duration.ofMillis(this.connectionTimeout.getMilliseconds()))
					.build();
		}
	}


	@Override
	public void shutdown()
	{
		connectionManager.shutdown();

		try
		{
			if (client != null)
				client.close();
		}
		catch (Throwable t)
		{
			log.error("Error shutting down shared ResteasyClient!", t);
		}

		try
		{
			if (clientH2C != null)
				clientH2C.close();
		}
		catch (Throwable t)
		{
			log.error("Error shutting down shared H2C ResteasyClient!", t);
		}
	}


	public interface AuthCredential
	{
		ResteasyClientFactoryImpl.AuthScope scope();
	}

	public static record AuthScope(String scheme, String host, int port)
	{
		boolean test(URI uri)
		{
			return test(uri.getScheme(), uri.getHost(), uri.getPort());
		}


		boolean test(String uriScheme, String uriHost, int uriPort)
		{
			if (scheme != null && !scheme.equalsIgnoreCase(uriScheme))
				return false;
			else if (port != -1 && uriPort != port)
				return false;
			else
				return host().equalsIgnoreCase(uriHost);
		}
	}


	public static record BearerTokenCredentials(AuthScope scope, BearerGenerator token) implements AuthCredential
	{

	}

	public static record UsernamePasswordCredentials(AuthScope scope, String username, String password,
	                                                 boolean preempt) implements AuthCredential
	{
	}
}
