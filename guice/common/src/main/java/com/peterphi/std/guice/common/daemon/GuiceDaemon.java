package com.peterphi.std.guice.common.daemon;

import com.google.inject.Inject;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.common.breaker.Breaker;
import com.peterphi.std.guice.common.breaker.BreakerService;
import com.peterphi.std.guice.common.breaker.DaemonBreaker;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.threading.Daemon;
import com.peterphi.std.threading.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class GuiceDaemon extends Daemon implements StoppableService, GuiceLifecycleListener
{
	private boolean daemonThread;

	@Inject
	ShutdownManager shutdownManager;

	@Inject
	GuiceDaemonRegistry registry;

	@Inject
	BreakerService breakerService;

	private List<String> breakerNames;

	private Breaker _breaker = null;

	public GuiceDaemon()
	{
		this(true);
	}


	public GuiceDaemon(boolean daemonThread)
	{
		this.daemonThread = daemonThread;

		this.breakerNames = new ArrayList<>(Arrays.asList("daemon", getName()));

		// Recognise additional breaker names
		if (getClass().isAnnotationPresent(DaemonBreaker.class))
		{
			final DaemonBreaker annotation = getClass().getAnnotation(DaemonBreaker.class);

			if (annotation.value().length > 0)
			{
				this.breakerNames.addAll(Arrays.asList(annotation.value()));
			}
		}
	}


	/**
	 * Lazy-create a Breaker representing all the breaker names.
	 *
	 * @return
	 * @see BreakerService#register(Consumer, List)
	 */
	public Breaker getBreaker()
	{
		if (_breaker == null)
		{
			_breaker = breakerService.register(this :: notifyBreakerChange, getBreakerNames());
		}

		return _breaker;
	}


	/**
	 * Method designed to be overridden to receive a notification any time the tripped state of the breaker returned by {@link
	 * #getBreaker()} changes. This method is executed synchronously with breaker evaluation potentially impacting multiple
	 * threads, so it should not block, nor should it perform any heavy computation
	 *
	 * @param tripped true if the breaker is tripped, otherwise false if the breaker is normal
	 */
	protected void notifyBreakerChange(final boolean tripped) {
		// No action required
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



	public List<String> getBreakerNames()
	{
		return this.breakerNames;
	}


	@Override
	protected String getThreadName()
	{
		return getName();
	}
}
