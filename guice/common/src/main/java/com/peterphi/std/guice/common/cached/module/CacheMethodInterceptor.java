package com.peterphi.std.guice.common.cached.module;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.common.cached.CacheManager;
import com.peterphi.std.guice.common.cached.annotation.Cache;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CacheMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = LoggerFactory.getLogger(CacheMethodInterceptor.class);

	/**
	 * Use a guava cache with soft values (so the GC can reclaim the space if necessary)
	 */
	private final com.google.common.cache.Cache<String, CacheResult> cache = CacheManager.build("Cache-annotated methods",
	                                                                                            CacheBuilder
			                                                                                            .newBuilder()
			                                                                                            .softValues());

	private final Meter hits;
	private final Meter misses;


	public CacheMethodInterceptor(MetricRegistry registry)
	{
		this.hits = registry.meter(GuiceMetricNames.CACHE_HITS);
		this.misses = registry.meter(GuiceMetricNames.CACHE_MISSES);

		CacheManager.register("Cache-annotated methods", cache);
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		if (invocation.getArguments().length > 0)
		{
			throw new IllegalArgumentException(
					"This caching interceptor does not support caching results of method calls with arguments");
		}


		final Cache options = invocation.getMethod().getAnnotation(Cache.class);

		final long timeout = options.timeout();

		final String key;
		if (StringUtils.isEmpty(options.name()))
		{
			key = invocation.getMethod().toGenericString() + "|" + invocation.getThis().hashCode();
		}
		else
		{
			key = options.name();
		}

		final CacheResult cacheResult = cache.getIfPresent(key);
		if (cacheResult != null)
		{
			//if we haven't hit the result's invalidation timeout
			if (cacheResult.expires >= System.currentTimeMillis())
			{
				if (log.isDebugEnabled())
					log.debug("Returning cached result for " + key);

				//return the previous result
				hits.mark();

				return cacheResult.result;
			}
		}

		//first time calling this method or previous result has timed out
		{
			Object result = invocation.proceed();

			CacheResult cr = new CacheResult(result, System.currentTimeMillis() + timeout);
			cache.put(key, cr);

			misses.mark();
			return result;
		}
	}


	private static final class CacheResult
	{
		private final Object result;
		private final long expires;


		public CacheResult(final Object result, final long expires)
		{
			this.result = result;
			this.expires = expires;
		}
	}
}
