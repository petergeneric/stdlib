package com.peterphi.servicemanager.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.net.NetworkConfig;

public class ResourceGuiceModule extends AbstractModule
{
	@Override
	protected void configure()
	{

	}


	@Provides
	@Named("template-config")
	public NetworkConfig getTemplateNetworkConfig()
	{
		NetworkConfig config = new NetworkConfig();

		config.path = "/resource-templates";
		config.properties = new GuiceConfig();

		return config;
	}
}
