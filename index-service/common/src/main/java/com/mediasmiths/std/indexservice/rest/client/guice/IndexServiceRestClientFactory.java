package com.mediasmiths.std.indexservice.rest.client.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.std.NotImplementedException;
import com.mediasmiths.std.guice.restclient.JAXRSProxyClientFactory;
import com.mediasmiths.std.guice.restclient.RestClientFactory;
import com.mediasmiths.std.indexservice.rest.client.IndexServiceClient;

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
