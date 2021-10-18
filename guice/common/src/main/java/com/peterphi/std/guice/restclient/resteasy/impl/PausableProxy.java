package com.peterphi.std.guice.restclient.resteasy.impl;

import com.peterphi.std.guice.restclient.exception.ServiceBreakerTripPreventsCallException;
import com.peterphi.std.threading.Deadline;
import com.peterphi.std.threading.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class PausableProxy implements InvocationHandler
{
	private static final Logger log = LoggerFactory.getLogger(PausableProxy.class);

	private static Method IS_IN_HTTP_CONTEXT;

	static
	{
		// If available, ty to use HttpCallContext to determine if we're operating within an Http Call
		try
		{
			IS_IN_HTTP_CONTEXT = Class.forName("com.peterphi.std.guice.web.HttpCallContext").getMethod("peek");
		}
		catch (ClassNotFoundException e)
		{
			IS_IN_HTTP_CONTEXT = null;
		}
		catch (Throwable t)
		{
			IS_IN_HTTP_CONTEXT = null;

			throw new IllegalArgumentException("Unable to access HttpCallContext.peek!", t);
		}
	}

	private static Timeout MAX_WAIT = new Timeout(2, TimeUnit.HOURS);
	private static Timeout MAX_WAIT_IF_HTTP_CONTEXT = new Timeout(30, TimeUnit.SECONDS);


	private final Object rest;
	private final AtomicInteger currentlyPausedCount;
	private boolean paused = false;
	private final boolean isFastFailServiceClient;


	public PausableProxy(final Object resteasyProxyClient,
	                     final boolean isFastFailServiceClient,
	                     final AtomicInteger currentlyPausedCount)
	{
		this.rest = resteasyProxyClient;
		this.currentlyPausedCount = currentlyPausedCount;
		this.isFastFailServiceClient = isFastFailServiceClient;
	}


	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		if (paused && method.getDeclaringClass() != Object.class)
		{
			pause(isFastFailServiceClient);
		}

		return method.invoke(rest, args);
	}


	/**
	 * Wait until unpaused (or until we hit the max wait timeout)
	 */
	private void pause(final boolean fastFail)
	{
		if (!fastFail)
		{
			final Deadline deadline = isHttpCall() ? MAX_WAIT_IF_HTTP_CONTEXT.start() : MAX_WAIT.start();

			currentlyPausedCount.incrementAndGet();

			try
			{
				while (paused && deadline.isValid())
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						return;
					}
				}
			}
			finally
			{
				currentlyPausedCount.decrementAndGet();
			}

			if (paused)
				log.warn("Hit timeout while waiting for service to be unpaused, will now throw exception...");
		}

		if (paused)
			throw new ServiceBreakerTripPreventsCallException(
					"Unable to make outgoing service call: breaker is still tripped after maximum wait");
	}


	private static boolean isHttpCall()
	{
		try
		{
			return IS_IN_HTTP_CONTEXT != null && IS_IN_HTTP_CONTEXT.invoke(null) != null;
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			return false;
		}
	}


	public void setPaused(final boolean val)
	{
		this.paused = val;
	}
}
