package com.peterphi.std.indexservice.rest.client.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.RestClientFactory;
import com.peterphi.std.indexservice.rest.client.IndexServiceClient;

import java.net.URI;

@Singleton
class IndexServiceRestClientFactory implements RestClientFactory
{
	protected final IndexServiceClient client;
	protected final JAXRSProxyClientFactory jaxClientFactory;

	@Inject
	public IndexServiceRestClientFactory(IndexServiceClient client, JAXRSProxyClientFactory jaxClientFactory)
	{
		this.client = client;
		this.jaxClientFactory = jaxClientFactory;
	}

	@Override
	public <T> T getClient(Class<T> iface)
	{
		final URI uri = client.findServiceEndpoint(iface);

		return jaxClientFactory.createClient(iface, uri);
	}

	@Override
	public <T> T getClient(Class<T> iface, String name)
	{
		throw new NotImplementedException("Cannot currently retrieve named services through the index service RestClientFactory");
	}

}
