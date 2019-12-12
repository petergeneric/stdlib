package com.peterphi.std.guice.common.ognl;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.common.cached.CacheManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class OgnlEvaluatorCollection
{
	private final Cache<String, OgnlEvaluator> interpreted = CacheManager.build("OgnlEvaluatorCollection",
	                                                                            CacheBuilder.newBuilder().softValues());

	private final ConcurrentHashMap<String, OgnlEvaluator> compiled = new ConcurrentHashMap<>();


	public synchronized OgnlEvaluator get(final String expression)
	{
		try
		{
			final OgnlEvaluator precompiled = compiled.get(expression);

			if (precompiled != null)
				return precompiled;
			else
				return interpreted.get(expression, () -> new OgnlEvaluator(expression, this :: notifyHasBeenCompiled));
		}
		catch (ExecutionException e)
		{
			throw new IllegalArgumentException("Error retrieving/constructing OgnlEvaluator!", e);
		}
	}


	public synchronized void notifyHasBeenCompiled(final String expression, final OgnlEvaluator evaluator)
	{
		compiled.put(expression, evaluator);

		// No longer need to hold a GCable reference, this has entered the permanent collection of compiled OGNL expressions
		interpreted.invalidate(expression);
	}


	@Override
	public String toString()
	{
		return MoreObjects
				       .toStringHelper(this)
				       .add("interpreted", interpreted.asMap().entrySet())
				       .add("compiled", compiled.entrySet())
				       .toString();
	}
}
