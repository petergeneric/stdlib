package com.peterphi.std.indexservice.rest.client.register;

import com.google.inject.Inject;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Daemon;
import com.peterphi.std.threading.Timeout;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class IndexServiceHeartbeater extends Daemon implements StoppableService
{
	private static final Logger log = Logger.getLogger(IndexServiceHeartbeater.class);

	private final IndexRegistrationHelper helper;
	private final String contextName;


	@Inject
	public IndexServiceHeartbeater(ShutdownManager manager, IndexRegistrationHelper helper, Configuration configuration)
	{
		this.helper = helper;
		this.contextName = configuration.getString(GuiceProperties.CONTEXT_NAME_PROPERTY);

		manager.register(this);

		startThread("IndexHeartbeat " + contextName);
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
				log.fatal("Error in IndexHeartbeat " + contextName + " : ", e);
				throw e;
			}
			catch (Exception e)
			{
				log.error("Ignoring exception in IndexHeartbeat " + contextName + ": ", e);
			}
			catch (Error e)
			{
				log.fatal("Error in IndexHeartbeat " + contextName + ": ", e);
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
