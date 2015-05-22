package com.peterphi.std.guice.common.cached.module;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.cached.annotation.Cache;

public class CacheModule extends AbstractModule
{
	private final MetricRegistry metrics;


	public CacheModule(final MetricRegistry metrics)
	{
		this.metrics = metrics;
	}


	@Override
	protected void configure()
	{
		CacheMethodInterceptor interceptor = new CacheMethodInterceptor(metrics);

		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cache.class), interceptor);
	}
}
