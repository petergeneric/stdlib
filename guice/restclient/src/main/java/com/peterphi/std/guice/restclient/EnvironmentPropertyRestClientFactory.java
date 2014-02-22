package com.peterphi.std.guice.restclient;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.peterphi.std.NotImplementedException;

import java.net.URI;

@Singleton
public class EnvironmentPropertyRestClientFactory implements RestClientFactory
{
	@Inject
	protected JAXRSProxyClientFactory proxyFactory;

	@Inject
	protected Injector injector;

	public EnvironmentPropertyRestClientFactory()
	{

	}

	@Override
	public <T> T getClient(Class<T> iface)
	{
		final String propertyName = "service." + iface.getSimpleName() + ".endpoint";

		Binding<URI> binding = injector.getBinding(Key.get(URI.class, Names.named(propertyName)));
		final URI endpoint = binding.getProvider().get();

		// TODO handle authentication (getExistingBinding on .user and .password?)

		return proxyFactory.createClient(iface, endpoint);
	}

	@Override
	public <T> T getClient(Class<T> iface, String name)
	{
		throw new NotImplementedException("Multiple instances of clients not currently supported");
	}
}
