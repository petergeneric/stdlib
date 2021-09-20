package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.converter.CommonTypesParamConverterProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Builds ResteasyClient objects
 */
@Singleton
public class ResteasyClientFactoryImpl implements StoppableService
{
	private static final Logger log = Logger.getLogger(ResteasyClientFactoryImpl.class);

	private final HttpClientFactory httpClientFactory;
	private final ResteasyProviderFactory resteasyProviderFactory;

	private ResteasyClient client;
	private ResteasyClient clientH2C;


	@Inject
	public ResteasyClientFactoryImpl(final ShutdownManager manager,
	                                 final TracingClientRequestFilter tracingRequestFilter,
	                                 final RemoteExceptionClientResponseFilter remoteExceptionClientResponseFilter,
	                                 final JAXBContextResolver jaxbContextResolver,
	                                 final HttpClientFactory httpClientFactory)
	{
		this.httpClientFactory = httpClientFactory;

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

		if (manager != null)
			manager.register(this);
	}


	public ResteasyClient getOrCreateClient(final AuthCredential credentials,
	                                        final boolean fastFail,
	                                        final boolean storeCookies,
	                                        final boolean h2c)
	{
		final boolean canShare = !fastFail && !storeCookies;

		// If possible, get a shared Http Client
		ClientHttpEngine shared = null;
		if (canShare)
			shared = httpClientFactory.getClient(h2c, fastFail, storeCookies);

		// We will try to share unauthenticated clients
		// This returns the existing client if present, otherwise sets up a Consumer to capture that client for future reuse
		Consumer<ResteasyClient> sharedClientSetter = null;
		if (shared != null && credentials == null)
		{
			if (h2c && httpClientFactory.willVaryWithH2C())
			{
				if (this.clientH2C != null)
					return this.clientH2C;
				else
					sharedClientSetter = (c) -> this.clientH2C = c;
			}
			else
			{
				if (this.client != null)
					return this.client;
				else
					sharedClientSetter = (c) -> this.client = c;
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
					final URI uri = ctx.getUri();
					if (credentials.scope().test(uri.getScheme(), uri.getHost(), uri.getPort()))
					{
						if (credentials instanceof BearerTokenCredentials)
						{
							ctx
									.getHeaders()
									.putIfAbsent("Authorization",
									             Collections.singletonList("Bearer " +
									                                       ((BearerTokenCredentials) credentials)
											                                       .token()
											                                       .getToken()));
						}
						else if (credentials instanceof UsernamePasswordCredentials)
						{
							UsernamePasswordCredentials passwd = (UsernamePasswordCredentials) credentials;
							final String headerVal = "Basic " +
							                         Base64.encodeBase64((passwd.username() + ":" + passwd.password()).getBytes(
									                         StandardCharsets.UTF_8));

							ctx.getHeaders().putIfAbsent("Authorization", Collections.singletonList(headerVal));
						}
					}
				}
			});
		}

		if (storeCookies)
			builder.enableCookieManagement();

		// Build and apply the HttpEngine
		if (shared != null)
			builder.httpEngine(shared); // Use the shared HTTP client
		else
			builder.httpEngine(httpClientFactory.getClient(h2c, fastFail, storeCookies)); // Get an appropriate HttpClient

		final ResteasyClient client = builder.build();

		if (sharedClientSetter != null)
			sharedClientSetter.accept(client);

		return client;
	}


	@Override
	public void shutdown()
	{
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

	public static class AuthScope
	{
		public final String scheme;
		public final String host;
		public final int port;


		public AuthScope(final String scheme, final String host, final int port)
		{
			this.scheme = scheme;
			this.host = host;
			this.port = port;
		}


		public boolean test(String uriScheme, String uriHost, int uriPort)
		{
			if (scheme != null && !scheme.equalsIgnoreCase(uriScheme))
				return false;
			else if (port != -1 && uriPort != port)
				return false;
			else
				return host().equalsIgnoreCase(uriHost);
		}


		public String scheme()
		{
			return scheme;
		}


		public String host()
		{
			return host;
		}


		public int port()
		{
			return port;
		}
	}


	public static class BearerTokenCredentials implements AuthCredential
	{
		public final AuthScope scope;
		public final BearerGenerator token;


		public BearerTokenCredentials(final AuthScope scope, final BearerGenerator token)
		{
			this.scope = scope;
			this.token = token;
		}


		@Override
		public AuthScope scope()
		{
			return scope;
		}


		public BearerGenerator token()
		{
			return token;
		}
	}

	public static class UsernamePasswordCredentials implements AuthCredential
	{
		public final AuthScope scope;
		public final String username;
		public final String password;
		public final boolean preempt;


		public UsernamePasswordCredentials(final AuthScope scope,
		                                   final String username,
		                                   final String password,
		                                   final boolean preempt)
		{
			this.scope = scope;
			this.username = username;
			this.password = password;
			this.preempt = preempt;
		}


		@Override
		public AuthScope scope()
		{
			return scope;
		}


		public String username()
		{
			return username;
		}


		public String password()
		{
			return password;
		}


		public boolean preempt()
		{
			return preempt;
		}
	}
}
