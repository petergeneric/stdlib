package com.peterphi.std.guice.apploader;

/**
 * A service that is registered with a {@link com.peterphi.std.guice.apploader.impl.GuiceRegistry} for configuration and lifecycle
 * event injection.
 */
public interface GuiceApplication
{
	/**
	 * Called when a new Injector has been created, after Guice injection has been applied to this instance.
	 * <p/>
	 * No guarantees are made about the order in which GuiceApplication instances will be called
	 */
	public void configured();

	/**
	 * Called when an Injector is stopping and before the ShutdownManager for the GuiceRegistry is signalled<br />
	 * The GuiceRegistry will wait for this method to return before proceeding. No guarantees are made about the order in which
	 * GuiceApplication instances will be called
	 */
	public void stopping();
}
