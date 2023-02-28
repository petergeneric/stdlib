package com.peterphi.std.guice.web.rest.resteasy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GuiceInitThreadWorker implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(GuiceInitThreadWorker.class);

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
