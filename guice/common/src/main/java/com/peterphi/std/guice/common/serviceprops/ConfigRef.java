package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.typed.TypedConfigRef;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class ConfigRef implements Provider<String>
{
	private final Configuration configuration;
	private final String name;


	public ConfigRef(final Configuration configuration, final String name)
	{
		this.configuration = configuration;
		this.name = name;
	}


	@Override
	public String get()
	{
		return configuration.getString(name);
	}


	public String[] getStringArray()
	{
		return configuration.getStringArray(name);
	}


	public List<Object> getList()
	{
		return configuration.getList(name);
	}


	public Object getObject()
	{
		return configuration.getProperty(name);
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
