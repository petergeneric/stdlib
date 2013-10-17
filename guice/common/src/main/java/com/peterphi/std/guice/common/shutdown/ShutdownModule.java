package com.peterphi.std.guice.common.shutdown;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;

/**
 * A Guice module that adds a Singleton {@link ShutdownManager} binding<br />
 * This module also exposes a shutdown method that triggers the shutdown of the associated ShutdownManager
 */
public class ShutdownModule extends AbstractModule
{
	private final ShutdownManager manager = new ShutdownManagerImpl();

	@Override
	protected void configure()
	{
	}

	@Singleton
	@Provides
	public ShutdownManager getShutdownManager()
	{
		return this.manager;
	}

	/**
	 * Triggers the shutdown action
	 */
	public void shutdown()
	{
		manager.shutdown();
	}
}
