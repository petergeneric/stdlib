package com.peterphi.std.guice.testrestclient.guice;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.testrestclient.server.ExampleThread;
import com.peterphi.std.guice.testrestclient.server.MyTestService;
import com.peterphi.std.guice.testrestclient.server.MyTestServiceImpl;

public class MyAppModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(MyTestService.class).to(MyTestServiceImpl.class);
		RestResourceRegistry.register(MyTestService.class);

		bind(ExampleThread.class).asEagerSingleton();
	}
}
