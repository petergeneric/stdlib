package com.peterphi.std.guice.common.daemon;

import com.google.inject.Inject;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Daemon;
import com.peterphi.std.threading.Timeout;

public abstract class GuiceDaemon extends Daemon implements StoppableService, GuiceLifecycleListener
{
	private boolean daemonThread;

	@Inject
	ShutdownManager shutdownManager;


	public GuiceDaemon()
	{
		this(true);
	}


	public GuiceDaemon(boolean daemonThread)
	{
		this.daemonThread = daemonThread;
	}


	@Override
	protected boolean shouldStartAsDaemon()
	{
		return daemonThread;
	}


	@Override
	public void postConstruct()
	{
		startThread();
		shutdownManager.register(this);
	}


	@Override
	public void shutdown()
	{
		stopThread();
	}

	/**
	 * Sleep for the specified amount of time (unless the daemon is stopping, in which case do not sleep at all). Returns
	 * immediately if the thread is interrupted.
	 *
	 * @param millis
	 * 		the amount of time to sleep for
	 */
	protected void sleep(long millis)
	{
		if (!isRunning() || millis <= 0)
			return;

		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			// ignore & return early
		}
	}


	/**
	 * Sleep for the specified amount of time (unless the daemon is stopping, in which case do not sleep at all). Returns
	 * immediately if the thread is interrupted.
	 *
	 * @param timeout
	 * 		the amount of time to sleep for
	 */
	protected void sleep(Timeout timeout)
	{
		if (!isRunning() || timeout.getMilliseconds() <= 0)
			return;

		sleep(timeout.getMilliseconds());
	}
}
