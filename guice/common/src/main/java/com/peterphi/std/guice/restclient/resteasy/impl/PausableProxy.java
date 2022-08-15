package com.peterphi.std.guice.restclient.resteasy.impl;

import com.peterphi.std.guice.common.breaker.Breaker;
import com.peterphi.std.guice.restclient.exception.ServiceBreakerTripPreventsCallException;
import com.peterphi.std.threading.Deadline;
import com.peterphi.std.threading.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.BadRequestException;
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
	private final boolean isFastFailServiceClient;
	private final Breaker breaker;


	public PausableProxy(final Object resteasyProxyClient,
	                     final boolean isFastFailServiceClient,
	                     final Breaker breaker,
	                     final AtomicInteger currentlyPausedCount)
	{
		this.rest = resteasyProxyClient;
		this.isFastFailServiceClient = isFastFailServiceClient;
		this.breaker = breaker;
		this.currentlyPausedCount = currentlyPausedCount;
	}


	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
	{
		if (isPaused() && method.getDeclaringClass() != Object.class)
		{
			pause(isFastFailServiceClient);
		}

		try
		{
			return method.invoke(rest, args);
		}
		catch (InvocationTargetException e)
		{
			final Throwable cause = e.getCause();

			// Make sure we never throw a BadRequestException, because this bubbles all the way up and throws a 400 Bad Request error against our own service if uncaught
			if (cause instanceof BadRequestException)
				throw new RuntimeException("Remote service returned 400 Bad Request!", e);
			else
				throw cause;
		}
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
				while (isPaused() && deadline.isValid())
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

			if (isPaused())
				log.warn("Hit timeout while waiting for service to be unpaused, will now throw exception...");
		}

		if (isPaused())
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


	private boolean isPaused()
	{
		return breaker != null && breaker.isTripped();
	}
}
