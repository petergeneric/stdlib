package com.peterphi.std.guice.common.retry.module;

import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.threading.retry.RetryManager;
import com.peterphi.std.threading.retry.backoff.ExponentialBackoff;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import java.util.Arrays;

final class RetryMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(RetryMethodInterceptor.class);

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		Retry options = invocation.getMethod().getAnnotation(Retry.class);

		RetryManager mgr = buildRetryManager(options);

		try
		{
			if (log.isTraceEnabled())
				log.trace("Attempting retryable invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()));

			return mgr.run(new InvocationRetryable(invocation, options.on(), options.exceptOn(), options.exceptOnCore()));
		}
		catch (Throwable t)
		{
			if (log.isTraceEnabled())
				log.trace("Retrying invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()) + " failed.", t);

			throw t;
		}
	}

	private RetryManager buildRetryManager(Retry options)
	{
		final Timeout initial = new Timeout(options.backoffTime(), options.backoffUnit());

		ExponentialBackoff backoff = new ExponentialBackoff(initial, options.backoffExponent());

		return new RetryManager(backoff, options.maxAttempts());
	}
}
