package com.mediasmiths.std.guice.common.metrics;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Fronts the Coda Hale Metrics MetricsRegistry instance to allow more
 */
@Singleton
public class StatsRegistry implements StoppableService
{
	private final MetricsRegistry registry;


	@Inject
	public StatsRegistry(ShutdownManager manager)
	{
		this.registry = new MetricsRegistry();

		manager.register(this);
	}


	/**
	 * Returns the underlying registry instance
	 *
	 * @return
	 */
	public MetricsRegistry getRegistry()
	{
		return registry;
	}


	/**
	 * Given a new {@link Gauge}, registers it under the given class and name.
	 *
	 * @param clazz
	 * 		the class which owns the metric
	 * @param name
	 * 		the name of the metric
	 * @param metric
	 * 		the metric
	 * @param <T>
	 * 		the type of the value returned by the metric
	 *
	 * @return {@code metric}
	 */
	public <T> Gauge<T> newGauge(Class<?> clazz, String name, Gauge<T> metric)
	{
		return registry.newGauge(clazz, name, metric);
	}


	/**
	 * Creates a new {@link Meter} and registers it under the given class and name.
	 *
	 * @param clazz
	 * 		the class which owns the metric
	 * @param name
	 * 		the name of the metric
	 * @param eventType
	 * 		the plural name of the type of events the meter is measuring (e.g., {@code
	 * 		"requests"})
	 * @param unit
	 * 		the rate unit of the new meter
	 *
	 * @return a new {@link Meter}
	 */
	public Meter newMeter(Class<?> clazz, String name, String eventType, TimeUnit unit)
	{
		return registry.newMeter(clazz, name, eventType, unit);
	}


	/**
	 * Creates a new non-biased {@link Histogram} and registers it under the given class and name.
	 *
	 * @param clazz
	 * 		the class which owns the metric
	 * @param name
	 * 		the name of the metric
	 *
	 * @return a new {@link Histogram}
	 */
	public Histogram newHistogram(Class<?> clazz, String name)
	{
		return registry.newHistogram(clazz, name);
	}


	/**
	 * Creates a new {@link Counter} and registers it under the given class and name.
	 *
	 * @param clazz
	 * 		the class which owns the metric
	 * @param name
	 * 		the name of the metric
	 *
	 * @return a new {@link Counter}
	 */
	public Counter newCounter(Class<?> clazz, String name)
	{
		return registry.newCounter(clazz, name);
	}


	/**
	 * Creates a new {@link Timer} and registers it under the given class and name, measuring
	 * elapsed time in milliseconds and invocations per second.
	 *
	 * @param clazz
	 * 		the class which owns the metric
	 * @param name
	 * 		the name of the metric
	 *
	 * @return a new {@link Timer}
	 */
	public Timer newTimer(Class<?> clazz, String name)
	{
		return registry.newTimer(clazz, name);
	}


	@Override
	public void shutdown()
	{
		registry.shutdown();
	}
}
