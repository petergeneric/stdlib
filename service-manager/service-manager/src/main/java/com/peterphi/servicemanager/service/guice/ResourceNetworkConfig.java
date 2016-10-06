package com.peterphi.servicemanager.service.guice;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.common.serviceprops.net.NetworkConfig;
import com.peterphi.std.guice.common.serviceprops.net.NetworkConfigReloadDaemon;

@EagerSingleton
public class ResourceNetworkConfig
{
	@Inject
	public void setup(NetworkConfigReloadDaemon daemon, @Named("template-config") NetworkConfig templateConfig)
	{
		daemon.register(templateConfig);
	}
}
