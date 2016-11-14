package com.peterphi.std.guice.common.daemon;

import com.google.inject.Inject;
import com.peterphi.std.annotation.ServiceName;
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

	@Inject
	GuiceDaemonRegistry registry;


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
		if (!isThreadRunning())
			startThread();

		shutdownManager.register(this);
		registry.register(this);
	}


	@Override
	public void shutdown()
	{
		if (isThreadRunning())
			stopThread();

		if (registry != null)
			registry.unregister(this);
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
			// Sleep until the timeout (or until someone wakes us)
			synchronized (this)
			{
				this.wait(millis);
			}
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
		sleep(timeout.getMilliseconds());
	}


	public String getName()
	{
		Class<?> clazz = getClass();

		// If we get a guice-enhanced class then we should go up one level to get the class name from the user's code
		if (clazz.getName().contains("$$EnhancerByGuice$$"))
			clazz = clazz.getSuperclass();

		if (clazz.isAnnotationPresent(ServiceName.class))
		{
			return clazz.getAnnotation(ServiceName.class).value();
		}
		else
			return clazz.getSimpleName();
	}


	@Override
	protected String getThreadName()
	{
		return getName();
	}
}
