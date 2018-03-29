package com.peterphi.std.guice.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.peterphi.std.guice.common.metrics.methodperf.MonitorPerformance;
import com.peterphi.std.util.tracing.Tracing;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

class MonitorPerformanceInterceptor implements MethodInterceptor
{
	private MetricRegistry registry;


	public MonitorPerformanceInterceptor(MetricRegistry registry)
	{
		this.registry = registry;
	}


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		final MonitorPerformance annotation = invocation.getMethod().getAnnotation(MonitorPerformance.class);

		final String operationId = Tracing.log("method:call", () -> annotation.name());

		final Timer.Context timer = registry.timer("method-timing." + annotation.name()).time();
		try
		{
			return invocation.proceed();
		}
		finally
		{
			if (timer != null)
				timer.stop();

			if (Tracing.isVerbose())
				Tracing.logOngoing(operationId, "method:finally", () -> annotation.name());
		}
	}
}
