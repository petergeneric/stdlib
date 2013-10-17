package com.peterphi.std.guice.common.retry.module;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.retry.annotation.Retry;

public class RetryModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		RetryMethodInterceptor interceptor = new RetryMethodInterceptor();

		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Retry.class), interceptor);
	}
}
