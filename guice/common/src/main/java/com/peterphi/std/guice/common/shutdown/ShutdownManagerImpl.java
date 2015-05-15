package com.peterphi.std.guice.common.shutdown;

import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Default implementation of a ShutdownManager, stores services in a Stack ready for shutdown
 */
class ShutdownManagerImpl implements ShutdownManager
{
	private static final Logger log = Logger.getLogger(ShutdownManagerImpl.class);

	private Stack<StoppableService> services = new Stack<StoppableService>();

	private boolean stopped = false;


	@Override
	public synchronized void register(StoppableService service)
	{
		if (service == null)
			throw new IllegalArgumentException("Must provide service to register!");
		if (stopped)
			throw new IllegalArgumentException("Cannot register for shutdown: manager already stopped");

		log.debug("Register for shutdown: " + service);

		services.push(service);
	}


	@Override
	public synchronized void shutdown()
	{
		if (stopped)
		{
			log.warn("Ignoring duplicate shutdown request");
		}

		// Allow services to shut down cleanly
		if (!services.isEmpty())
		{
			int failures = 0; // count the number of services that failed to shutdown

			log.info("Shutting down " + services.size() + " service(s)");

			// First, notify all services about the impending shutdown
			// We can't iterate across the stack in the right order without doing so destructively, so we create a copy as a list
			// And then reverse it to mimic the stack being iterated across using the right order
			{
				final List<StoppableService> toNotify = new ArrayList<>(services);

				for (StoppableService service : toNotify)
				{
					try
					{
						service.preShutdown();
					}
					catch (Throwable t)
					{
						failures++;
						log.warn("Pre-Shutdown failed for " + service + ":" + t.getMessage(), t);
					}
				}
			}

			// Now, shutdown all the services, unregistering them from the service stack at the same time
			while (!services.empty())
			{
				final StoppableService service = services.pop();

				try
				{
					log.debug("Requesting shutdown of " + service);

					service.shutdown();
				}
				catch (Throwable t)
				{
					failures++;
					log.warn("Shutdown failed for " + service + ": " + t.getMessage(), t);
				}
			}

			if (failures == 0)
			{
				log.info("Shutdown complete");
			}
			else
			{
				log.warn("Shutdown completed, " + failures + " service(s) threw an exception during shutdown");
			}

			stopped = true;
			services.clear();
		}
	}
}
