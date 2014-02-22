package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.GuiceApplication;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Static registry that holds the Injector and the applications currently registered to be injected with the latest Guice
 * objects.<br />
 * Uses {@link GuiceInjectorBootstrap} to acquire a {@link GuiceSetup} which defines the Guice Modules to use to build this
 * environment.
 */
public class GuiceRegistry
{
	private static final Logger log = Logger.getLogger(GuiceRegistry.class);
	private static final Object monitor = new Object();

	private static Set<GuiceApplication> services = new HashSet<GuiceApplication>();
	private static Injector injector;

	/**
	 * Request that an application be registered with this GuiceRegistry
	 *
	 * @param service
	 * 		the service to register (must not be null)
	 * @param durable
	 * 		true if the application registration should be durable (durable applications receive lifecycle updates and are
	 * 		reconfigured should {@link GuiceRegistry#restart} be called
	 */
	public static synchronized void register(GuiceApplication service, boolean durable)
	{
		final Injector injector = getInjector();

		injector.injectMembers(service);

		if (durable)
		{
			services.add(service);
		}

		service.configured();
	}

	public static Injector getInjector()
	{
		if (injector == null)
		{
			synchronized (monitor)
			{
				if (injector == null)
				{
					log.info("Trying to create Guice Injector...");
					injector = GuiceInjectorBootstrap.createInjector();
				}
			}
		}

		return injector;
	}

	/**
	 * Shutdown all services
	 */
	public static synchronized void stop()
	{
		if (injector != null)
		{
			// Shutdown the services first
			for (GuiceApplication service : services)
			{
				try
				{
					service.stopping();
				}
				catch (Throwable t)
				{
					log.warn("Error shutting down service " + service + ": " + t.getMessage(), t);
				}
			}

			// Now shutdown the environment
			ShutdownManager manager = injector.getInstance(ShutdownManager.class);
			manager.shutdown();

			// Allow the environment to be garbage collected
			injector = null;
		}
	}

	/**
	 * Restart Guice services
	 */
	public static synchronized void restart()
	{
		// Bring down the existing environment
		stop();

		// Start the environment
		try
		{
			// Bring up the new environment by reconfiguring the services
			for (GuiceApplication service : services)
			{
				register(service, true);
			}
		}
		catch (RuntimeException e)
		{
			log.warn("Failed to restart: " + e.getMessage(), e);
			stop();

			throw e;
		}
		catch (Error e)
		{
			log.warn("Failed to restart: " + e.getMessage(), e);
			stop();

			throw e;
		}
	}
}
