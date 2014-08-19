package com.peterphi.std.guice.testing;

import com.google.inject.Provider;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;

class MockProvider<T> implements Provider<T>
{
	private final Class<T> clazz;
	private final String name;


	public MockProvider(final Class<T> clazz, final String name)
	{
		this.clazz = clazz;
		this.name = name;
	}


	@Override
	public T get()
	{
		if (StringUtils.isEmpty(name))
			return Mockito.mock(clazz);
		else
			return Mockito.mock(clazz, name);
	}
}
