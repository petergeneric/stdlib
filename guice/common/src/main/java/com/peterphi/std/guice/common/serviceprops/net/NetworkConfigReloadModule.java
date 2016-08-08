package com.peterphi.std.guice.common.serviceprops.net;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.peterphi.std.guice.config.rest.iface.ConfigRestService;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;

public class NetworkConfigReloadModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(NetworkConfigReloadDaemon.class).asEagerSingleton();
	}


	@Provides
	@Singleton
	public ConfigRestService getConfigService(JAXRSProxyClientFactory factory)
	{
		return factory.getClient(ConfigRestService.class);
	}
}
