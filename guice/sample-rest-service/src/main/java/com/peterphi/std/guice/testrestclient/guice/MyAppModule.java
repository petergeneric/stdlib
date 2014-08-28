package com.peterphi.std.guice.testrestclient.guice;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.testrestclient.server.ExampleThread;

public class MyAppModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ExampleThread.class).asEagerSingleton();
	}
}
