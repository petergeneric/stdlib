package com.peterphi.std.guice.testrestclient.server;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Daemon;
import com.peterphi.std.threading.Timeout;

@Singleton
public class ExampleThread extends Daemon implements StoppableService
{
	private static final Logger log = Logger.getLogger(ExampleThread.class);

	private final Timeout sleepTime = new Timeout(5, TimeUnit.HOURS);

	@Inject
	public ExampleThread(ShutdownManager manager)
	{
		startThread();

		manager.register(this); // register for shutdown notifications
	}

	@Override
	protected boolean shouldStartAsDaemon()
	{
		return true;
	}

	@Override
	public void run()
	{
		while (isRunning())
		{
			log.info("ExampleThread would do some work now");

			sleepTime.sleep();
		}
	}

	@Override
	public void shutdown()
	{
		stopThread();
	}

}
