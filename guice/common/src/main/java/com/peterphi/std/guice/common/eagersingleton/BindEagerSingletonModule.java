package com.peterphi.std.guice.common.eagersingleton;

import com.google.inject.AbstractModule;

import java.util.List;

class BindEagerSingletonModule extends AbstractModule
{
	private final List<Class<?>> classes;


	public BindEagerSingletonModule(final List<Class<?>> classes)
	{
		this.classes = classes;
	}


	@Override
	protected void configure()
	{
		for (Class<?> clazz : classes)
			bind(clazz).asEagerSingleton();
	}
}
