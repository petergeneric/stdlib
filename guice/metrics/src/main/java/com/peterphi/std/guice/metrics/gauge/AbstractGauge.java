package com.peterphi.std.guice.metrics.gauge;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;

/**
 * Abstract parent class for Gauges. Should be bound as an eager singleton. When created the gauge will be auto-registered with
 * Guice (this behaviour can be customised by implementing logic in {@link #enabled()}. If enabled returns false then the Gauge
 * will not be registered with guice)
 */
public abstract class AbstractGauge implements Gauge<Long>, GuiceLifecycleListener
{
	@Inject
	MetricRegistry registry;


	public abstract String getName();


	/**
	 * Called once at startup after guice has finished building the object. If this method returns false then the object will not
	 * be registered with the {@link com.codahale.metrics.MetricRegistry}
	 *
	 * @return true by default if not overridden
	 */
	protected boolean enabled()
	{
		return true;
	}


	@Override
	public void postConstruct()
	{
		if (enabled())
		{
			registry.register(getName(), this);
		}
	}
}
