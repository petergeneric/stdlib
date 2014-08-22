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


	public static MetricRegistry buildRegistry()
	{
		MetricRegistry registry = new MetricRegistry();

		registry.register(MetricRegistry.name("jvm", "gc"), new GarbageCollectorMetricSet());
		registry.register(MetricRegistry.name("jvm", "memory"), new MemoryUsageGaugeSet());
		registry.register(MetricRegistry.name("jvm", "thread-states"), new ThreadStatesGaugeSet());
		registry.register(MetricRegistry.name("jvm", "fd", "usage"), new FileDescriptorRatioGauge());

		return registry;
	}
}
