package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.typed.TypedConfigRef;

import java.util.List;

public class ConfigRef implements Provider<String>
{
	private final GuiceConfig configuration;
	private final String name;


	public ConfigRef(final GuiceConfig configuration, final String name)
	{
		this.configuration = configuration;
		this.name = name;
	}


	public String getName()
	{
		return name;
	}


	@Override
	public String get()
	{
		return configuration.get(name);
	}


	public String getRaw()
	{
		return configuration.getRaw(name, null);
	}


	public List<String> getList()
	{
		return configuration.getList(name, null);
	}


	public <T> T get(Class<T> clazz)
	{
		return as(clazz).get();
	}


	public <T> TypedConfigRef<T> as(final Class<T> clazz)
	{
		return new TypedConfigRef<>(this, clazz);
	}
}
