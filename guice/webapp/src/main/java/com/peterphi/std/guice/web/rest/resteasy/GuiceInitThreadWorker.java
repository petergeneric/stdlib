package com.peterphi.std.guice.web.rest.resteasy;

import org.apache.log4j.Logger;

class GuiceInitThreadWorker implements Runnable
{
	private static final Logger log = Logger.getLogger(GuiceInitThreadWorker.class);

	private final GuicedResteasy dispatcher;

	public GuiceInitThreadWorker(GuicedResteasy dispatcher)
	{
		this.dispatcher = dispatcher;
	}

	public void run()
	{
		log.trace("Initialising ResteasyDispatcher...");
		try
		{
			dispatcher.initialise();
		}
		catch (Exception e)
		{
			log.error(dispatcher.getWebappPath() + " - eager startup failure; will try again on first call", e);
		}
	}
}
