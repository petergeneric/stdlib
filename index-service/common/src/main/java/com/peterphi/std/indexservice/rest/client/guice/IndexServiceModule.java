package com.peterphi.std.indexservice.rest.client.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.indexservice.rest.client.register.IndexServiceHeartbeater;
import com.peterphi.std.indexservice.rest.iface.IndexRestService;

import java.net.URI;

public class IndexServiceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(IndexServiceHeartbeater.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	public IndexRestService getIndexService(@Named("service.IndexRestService.endpoint") URI endpoint,
	                                        JAXRSProxyClientFactory clientFactory)
	{
		return clientFactory.createClient(IndexRestService.class, endpoint);
	}
}
