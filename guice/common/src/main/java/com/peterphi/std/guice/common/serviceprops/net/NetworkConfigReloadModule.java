package com.peterphi.std.guice.common.serviceprops.net;

import com.google.inject.AbstractModule;

public class NetworkConfigReloadModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(NetworkConfigReloadDaemon.class).asEagerSingleton();
	}
}
