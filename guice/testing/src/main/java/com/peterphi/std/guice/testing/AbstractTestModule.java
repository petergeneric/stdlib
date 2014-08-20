package com.peterphi.std.guice.testing;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 *
 */
public abstract class AbstractTestModule extends AbstractModule
{
	/**
	 * Create a binding for the provided class as a mockito mock
	 *
	 * @param clazz
	 * @param <T>
	 */
	protected <T> void mock(Class<T> clazz)
	{
		bind(clazz).toProvider(new MockProvider<T>(clazz, null)).in(Scopes.SINGLETON);
	}


	/**
	 * Create a named binding for the provided class as a mockito mock
	 *
	 * @param name
	 * 		shorthand for specifying a {@link com.google.inject.name.Named} binding
	 * @param clazz
	 * @param <T>
	 */
	protected <T> void bindNamedMock(String name, Class<T> clazz)
	{
		bindMock(Key.get(clazz, Names.named(name)), clazz, name);
	}


	/**
	 * Create a custom binding for the provided class as a mockito mock
	 *
	 * @param key
	 * 		the key to use for the binding
	 * @param clazz
	 * 		the class to mock
	 * @param mockName
	 * 		the name for the mockito mock (<strong>note:</strong> not the name of the Guice binding. If none is specified then a
	 * 		default name will be created based on the <code>key</code>
	 * @param <T>
	 */
	protected <T> void bindMock(Key<T> key, Class<T> clazz, String mockName)
	{
		if (mockName == null)
			mockName = "Guice binding " + key;

		bind(key).toProvider(new MockProvider<T>(clazz, mockName)).in(Scopes.SINGLETON);
	}
}
