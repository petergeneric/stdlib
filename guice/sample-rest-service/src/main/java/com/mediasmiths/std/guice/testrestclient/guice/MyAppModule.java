package com.mediasmiths.std.guice.testrestclient.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.guice.testrestclient.server.ExampleThread;
import com.mediasmiths.std.guice.testrestclient.server.MyTestService;
import com.mediasmiths.std.guice.testrestclient.server.MyTestServiceImpl;

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
