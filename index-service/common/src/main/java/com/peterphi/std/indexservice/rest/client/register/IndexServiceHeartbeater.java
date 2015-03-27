package com.peterphi.std.indexservice.rest.client.register;

import com.google.inject.Inject;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.indexservice.rest.type.UnregisterResponse;
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
		this.contextName = configuration.getString(GuiceProperties.SERVLET_CONTEXT_NAME);

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
				log.fatal("Illegal state exception in IndexHeartbeat " + contextName + " : ", e);
			}
			catch (Exception e)
			{
				log.error("Ignoring exception in IndexHeartbeat " + contextName + ": ", e);
			}
			catch (Error e)
			{
				log.fatal("Error in IndexHeartbeat " + contextName + ": ", e);
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
		try {
			UnregisterResponse response = helper.unregister();
		}
		catch(Exception e) {
			log.warn("Failed to eagerly unregister from Index Server",e);
		}
	}
}