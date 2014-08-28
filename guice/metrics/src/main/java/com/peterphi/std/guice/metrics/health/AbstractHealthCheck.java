package com.peterphi.std.guice.metrics.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.metrics.rest.types.HealthImplication;

/**
 * Abstract parent class for Health Checks. Should be bound as an eager singleton. When created the Health Check will be
 * auto-registered with
 * Guice (this behaviour can be customised by implementing logic in {@link #enabled()}. If enabled returns false then the Health
 * Check
 * will not be registered with guice)
 */
public abstract class AbstractHealthCheck extends HealthCheck implements GuiceLifecycleListener
{
	@Inject
	HealthCheckRegistry healthCheckRegistry;


	protected abstract String getName();


	protected HealthImplication getHealthImplication()
	{
		return HealthImplication.FATAL;
	}


	/**
	 * Called once at startup after guice has finished building the object. If this method returns false then the object will not
	 * be registered with the {@link com.codahale.metrics.health.HealthCheckRegistry}
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
			healthCheckRegistry.register(getHealthImplication() + ":" + getName(), this);
		}
	}
}
