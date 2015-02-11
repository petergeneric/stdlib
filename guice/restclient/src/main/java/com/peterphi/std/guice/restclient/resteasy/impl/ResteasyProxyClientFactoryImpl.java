package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.net.URI;
import java.util.Arrays;

@Singleton
public class ResteasyProxyClientFactoryImpl implements JAXRSProxyClientFactory
{
	@Inject
	ResteasyClientFactoryImpl clientFactory;

	@Inject
	Configuration config;


	@Override
	public ResteasyWebTarget getWebTarget(final String... names)
	{
		for (String name : names)
		{
			final String endpoint = config.getString("service." + name + ".endpoint", null);

			if (endpoint == null)
				continue;

			final URI uri = URI.create(endpoint);

			final String username = config.getString("service." + name + ".username", getUsername(uri));
			final String password = config.getString("service." + name + ".password", getPassword(uri));
			final boolean fastFail = config.getBoolean("service." + name + ".fast-fail", false);

			// TODO allow other per-service configuration?

			return createWebTarget(uri, fastFail, username, password);
		}

		throw new IllegalArgumentException("Cannot find service in configuration by any of these names: " + Arrays.asList(names));
	}


	@Override
	public <T> T getClient(final Class<T> iface, final String... names)
	{
		return getWebTarget(names).proxy(iface);
	}


	@Override
	public <T> T getClient(final Class<T> iface)
	{
		// TODO allow a service to annotate itself with configurable names?
		return getClient(iface, iface.getName(), iface.getSimpleName());
	}


	@Override
	public ResteasyWebTarget createWebTarget(final URI endpoint, String username, String password)
	{
		return createWebTarget(endpoint, false, username, password);
	}


	public ResteasyWebTarget createWebTarget(final URI endpoint, boolean fastFail, String username, String password)
	{
		if (username != null || password != null)
		{
			final AuthScope scope = new AuthScope(endpoint.getHost(), AuthScope.ANY_PORT);
			final Credentials credentials = new UsernamePasswordCredentials(endpoint.getAuthority());

			return clientFactory.getOrCreateClient(fastFail, scope, credentials, null).target(endpoint);
		}
		else
			return clientFactory.getOrCreateClient(fastFail, null, null, null).target(endpoint);
	}


	@Override
	public <T> T createClient(final Class<T> iface, final String endpoint)
	{
		return createClient(iface, URI.create(endpoint));
	}


	@Override
	public <T> T createClient(Class<T> iface, URI endpoint)
	{
		return createClientWithPasswordAuth(iface, endpoint, getUsername(endpoint), getPassword(endpoint));
	}


	@Override
	public <T> T createClientWithPasswordAuth(Class<T> iface, URI endpoint, String username, String password)
	{
		final boolean fastFail = iface.isAnnotationPresent(FastFailServiceClient.class);

		return createWebTarget(endpoint, fastFail, username, password).proxy(iface);
	}


	private static String getUsername(URI endpoint)
	{
		final String info = endpoint.getUserInfo();

		if (StringUtils.isEmpty(info))
			return null;
		else if (info.indexOf(':') != -1)
			return info.split(":", 2)[0];
		else
			return null;
	}


	private static String getPassword(URI endpoint)
	{
		final String info = endpoint.getUserInfo();

		if (StringUtils.isEmpty(info))
			return null;
		else if (info.indexOf(':') != -1)
			return info.split(":", 2)[1];
		else
			return null;
	}
}
