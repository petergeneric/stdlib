package com.peterphi.std.guice.metrics;

import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.log4j.InstrumentedAppender;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.log4j.LogManager;

import java.util.concurrent.TimeUnit;

/**
 * Fronts the Coda Hale Metrics MetricRegistry instance to allow more
 */
@Singleton
public class StatsRegistry implements StoppableService
{
	private final MetricRegistry registry;


	@Inject
	public StatsRegistry(ShutdownManager manager)
	{
		this.registry = new MetricRegistry();
		registry.register(MetricRegistry.name("jvm", "gc"), new GarbageCollectorMetricSet());
		registry.register(MetricRegistry.name("jvm", "memory"), new MemoryUsageGaugeSet());
		registry.register(MetricRegistry.name("jvm", "thread-states"), new ThreadStatesGaugeSet());
		registry.register(MetricRegistry.name("jvm", "fd", "usage"), new FileDescriptorRatioGauge());

		InstrumentedAppender log4jmetrics = new InstrumentedAppender(registry);
		log4jmetrics.activateOptions();
		LogManager.getRootLogger().addAppender(log4jmetrics);

		manager.register(this);
	}


	/**
	 * Returns the underlying registry instance
	 *
	 * @return
	 */
	public MetricRegistry getRegistry()
	{
		return registry;
	}


	@Override
	public void shutdown()
	{

	}
}
