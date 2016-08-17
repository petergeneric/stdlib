package com.peterphi.std.guice.common.retry.module;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import com.peterphi.std.guice.common.retry.retry.RetryManager;
import com.peterphi.std.guice.common.retry.retry.backoff.ExponentialBackoff;
import com.peterphi.std.threading.Timeout;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import java.util.Arrays;

final class RetryMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(RetryMethodInterceptor.class);

	private final Timer calls;
	private final Timer attempts;
	private final Meter attemptFailures;
	private final Meter totalFailures;


	public RetryMethodInterceptor(MetricRegistry registry)
	{
		this.calls = registry.timer(GuiceMetricNames.RETRY_CALLS);
		this.attempts = registry.timer(GuiceMetricNames.RETRY_ATTEMPTS);

		this.attemptFailures = registry.meter(GuiceMetricNames.RETRY_ATTEMPT_FAILURES);
		this.totalFailures = registry.meter(GuiceMetricNames.RETRY_TOTAL_FAILURES);
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		Timer.Context timer = this.calls.time();

		try
		{
			final Retry options = invocation.getMethod().getAnnotation(Retry.class);
			final RetryManager mgr = buildRetryManager(options);

			if (log.isTraceEnabled())
				log.trace("Attempting retryable invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " +
				          invocation.getThis() +
				          " with " +
				          Arrays.asList(invocation.getArguments()));

			return mgr.run(new InvocationRetryable(invocation,
			                                       options.on(),
			                                       options.exceptOn(),
			                                       options.exceptOnCore(),
			                                       options.exceptOnRestExceptionCodes()));
		}
		catch (Throwable t)
		{
			totalFailures.mark();

			if (log.isTraceEnabled())
				log.trace("Retrying invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " +
				          invocation.getThis() +
				          " with " +
				          Arrays.asList(invocation.getArguments()) +
				          " failed.", t);

			throw t;
		}
		finally
		{
			timer.stop();
		}
	}


	private RetryManager buildRetryManager(Retry options)
	{
		final Timeout initial = new Timeout(options.backoffTime(), options.backoffUnit());

		ExponentialBackoff backoff = new ExponentialBackoff(initial, options.backoffExponent());

		return new RetryManager(backoff, options.maxAttempts(), attempts, attemptFailures);
	}
}
