package com.peterphi.std.indexservice.rest.client.register;

import com.google.inject.Inject;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.serviceregistry.ApplicationContextNameRegistry;
import com.peterphi.std.threading.Daemon;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

public class IndexServiceHeartbeater extends Daemon implements StoppableService
{
	private static final Logger log = Logger.getLogger(IndexServiceHeartbeater.class);

	private final IndexRegistrationHelper helper;


	@Inject
	public IndexServiceHeartbeater(ShutdownManager manager, IndexRegistrationHelper helper)
	{
		this.helper = helper;

		manager.register(this);

		startThread("IndexHeartbeat " + ApplicationContextNameRegistry.getContextName());
	}


	@Override
	public void run()
	{
		while (isRunning())
		{
			try
			{
				final Timeout sleepTime = helper.pulse();

				sleepTime.sleep();
			}
			catch (IllegalStateException e)
			{
				log.fatal("Error in IndexHeartbeat " + ApplicationContextNameRegistry.getContextName() + " : ", e);
				throw e;
			}
			catch (Exception e)
			{
				log.error("Ignoring exception in IndexHeartbeat " + ApplicationContextNameRegistry.getContextName() + ": ", e);
			}
			catch (Error e)
			{
				log.fatal("Error in IndexHeartbeat " + ApplicationContextNameRegistry.getContextName() + ": ", e);
				throw e;
			}
		}
	}


	@Override
	protected boolean shouldStartAsDaemon()
	{
		return true;
	}


	@Override
	public void shutdown()
	{
		this.stopThread();

		// Eagerly unregister (lest the HTTP client be shut down and we be unable to register)
		helper.unregister();

		// Wait until the Thread terminates (or at most 60 seconds)
		Daemon.waitForTermination(this, 60 * 1000);
	}
}
