package com.peterphi.std.guice.common.cached.module;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.guice.common.cached.annotation.Cache;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

final class CacheMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(CacheMethodInterceptor.class);

	Map<String, CacheResult> results = new HashMap<>();

	private final Meter hits;
	private final Meter misses;


	public CacheMethodInterceptor(MetricRegistry registry)
	{
		this.hits = registry.meter(GuiceMetricNames.CACHE_HITS);
		this.misses = registry.meter(GuiceMetricNames.CACHE_MISSES);
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{

		if (invocation.getArguments().length > 0)
		{
			throw new IllegalArgumentException("This caching interceptor does not support caching results of method calls with arguments");
		}


		final Cache options = invocation.getMethod().getAnnotation(Cache.class);

		long timeout = options.timeout();

		final String key;
		if (StringUtils.isEmpty(options.name()))
		{
			key = invocation.getMethod().toGenericString();
		}
		else
		{
			key = options.name();
		}

		if (results.containsKey(key))
		{
			CacheResult cacheResult = results.get(key);

			//if we haven't hit the result's invalidation timeout
			if (cacheResult.time.withDurationAdded(timeout, 1).isAfterNow())
			{
				log.debug("Returning cached result for " + key);
				//return the previous result
				hits.mark();
				return cacheResult.result;
			}
		}

		//first time calling this method or previous result has timed out
		{
			Object result = invocation.proceed();
			DateTime now = DateTime.now();

			CacheResult cr = new CacheResult(result, now);
			results.put(key, cr);

			misses.mark();
			return result;
		}
	}


	class CacheResult
	{

		private final Object result;
		private final DateTime time;


		public CacheResult(final Object result, final DateTime time)
		{
			this.result = result;
			this.time = time;
		}
	}
}
