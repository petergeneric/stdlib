package com.peterphi.std.guice.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.peterphi.std.guice.common.metrics.methodperf.MonitorPerformance;

public class CoreMetricsModule extends AbstractModule
{
	private final MetricRegistry registry;


	public CoreMetricsModule(final MetricRegistry registry)
	{
		this.registry = registry;
	}


	@Override
	protected void configure()
	{
		bindInterceptor(Matchers.any(),
		                Matchers.annotatedWith(MonitorPerformance.class),
		                new MonitorPerformanceInterceptor(registry));
	}


	@Provides
	@Singleton
	public MetricRegistry getMetricRegistry()
	{
		return registry;
	}


	@Provides
	@Singleton
	public HealthCheckRegistry getHealthCheckRegistry()
	{
		return new HealthCheckRegistry();
	}


	public static MetricRegistry buildRegistry(final boolean includeJvm)
	{
		MetricRegistry registry = new MetricRegistry();

		if (includeJvm)
		{
			registry.register(MetricRegistry.name("jvm", "gc"), new GarbageCollectorMetricSet());
			registry.register(MetricRegistry.name("jvm", "memory"), new MemoryUsageGaugeSet());
			registry.register(MetricRegistry.name("jvm", "thread-states"), new ThreadStatesGaugeSet());
			registry.register(MetricRegistry.name("jvm", "fd", "usage"), new FileDescriptorRatioGauge());
		}

		return registry;
	}
}
