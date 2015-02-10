package com.peterphi.std.guice.apploader.impl;

import com.google.inject.AbstractModule;

class GuiceRegistryModule extends AbstractModule
{
	private final GuiceRegistry registry;


	public GuiceRegistryModule(final GuiceRegistry registry)
	{
		this.registry = registry;
	}


	@Override
	protected void configure()
	{
		bind(GuiceRegistry.class).toInstance(registry);
	}
}
