package com.peterphi.std.guice.common.retry.module;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.retry.annotation.Retry;

public class RetryModule extends AbstractModule
{
	private final MetricRegistry metrics;


	public RetryModule(final MetricRegistry metrics)
	{
		this.metrics = metrics;
	}


	@Override
	protected void configure()
	{
		RetryMethodInterceptor interceptor = new RetryMethodInterceptor(metrics);

		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Retry.class), interceptor);
	}
}
