package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import java.net.URI;

public class ResteasyProxyClientFactoryImpl implements JAXRSProxyClientFactory
{
	@Inject
	ResteasyClientFactoryImpl clientFactory;


	@Override
	public <T> T createClient(final Class<T> iface, final String endpoint)
	{
		return createClient(iface, URI.create(endpoint));
	}


	@Override
	public <T> T createClient(Class<T> iface, URI endpoint)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		// TODO query the service.properties too?

		if (!StringUtils.isNotEmpty(endpoint.getAuthority()) && !fastFail)
		{
			// Create a specific AuthScope and credential based on the authority of the URI
			final AuthScope scope = new AuthScope(endpoint.getHost(), AuthScope.ANY_PORT);
			final Credentials credentials = new UsernamePasswordCredentials(endpoint.getAuthority());

			return createClient(iface, endpoint, scope, credentials);
		}
		else
		{
			// Regular timeout, unauthenticated service; use shared unauthenticated client
			return clientFactory.getClient().target(endpoint).proxy(iface);
		}
	}


	@Override
	public <T> T createClientWithPasswordAuth(Class<T> iface, URI endpoint, String username, String password)
	{
		return createClient(iface, endpoint, AuthScope.ANY, new UsernamePasswordCredentials(username, password));
	}


	protected <T> T createClient(Class<T> iface, URI endpoint, AuthScope authScope, Credentials credentials)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		return clientFactory.newClient(fastFail, authScope, credentials).target(endpoint).proxy(iface);
	}
}
