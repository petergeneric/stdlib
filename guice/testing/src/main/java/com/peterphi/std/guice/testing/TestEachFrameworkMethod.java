package com.peterphi.std.guice.testing;

import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A FrameworkMethod implementation for a method annotated with {@link com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.TestEach},
 * specifying which collection indexes to use for this particular invocation of the method
 */
class TestEachFrameworkMethod extends FrameworkMethod
{
	private final Map<Integer, Integer> collectionIndexesByParameterIndex;


	public TestEachFrameworkMethod(final Method method, final Map<Integer, Integer> collectionIndexesByParameterIndex)
	{
		super(method);

		this.collectionIndexesByParameterIndex = collectionIndexesByParameterIndex;
	}


	public int getCollectionIndexForParameter(final int paramIndex)
	{
		final Integer ret = collectionIndexesByParameterIndex.get(paramIndex);

		if (ret != null)
			return ret;
		else
			throw new IllegalArgumentException("Do not have a collection index for requested param index " +
			                                   paramIndex +
			                                   " of " +
			                                   getMethod());
	}
}
