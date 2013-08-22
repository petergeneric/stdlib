package com.mediasmiths.std.guice.common.retry.module;

import com.mediasmiths.std.threading.retry.Retryable;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

final class InvocationRetryable implements Retryable<Object>
{
	private static final Logger log = Logger.getLogger(InvocationRetryable.class);

	final MethodInvocation invocation;
	final Class<? extends Throwable>[] alwaysRetry;
	final Class<? extends Throwable>[] noRetry;
	final Class<? extends Throwable>[] noRetryCore;

	public InvocationRetryable(MethodInvocation invocation,
	                           final Class<? extends Throwable>[] alwaysRetry,
	                           final Class<? extends Throwable>[] noRetry,
	                           final Class<? extends Throwable>[] noRetryCore)
	{
		this.invocation = invocation;
		this.alwaysRetry = alwaysRetry;
		this.noRetry = noRetry;
		this.noRetryCore = noRetryCore;
	}


	@Override
	public Object attempt(final int attempt) throws Exception
	{
		try
		{
			if (log.isTraceEnabled())
				log.trace("Invoking " + this.toString() + " for attempt #" + attempt);

			return invocation.proceed();
		}
		catch (Exception e)
		{
			throw e;
		}
		catch (Error e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean shouldRetry(final int attempt, final Throwable e)
	{
		// Throw if the type is in alwaysRetry
		for (Class<? extends Throwable> type : alwaysRetry)
		{
			if (type.isInstance(e))
				return true;
		}


		// Don't throw if the type is in noRetry
		for (Class<? extends Throwable> type : noRetry)
		{
			if (type.isInstance(e))
				return false;
		}

		// Don't throw if the type is in noRetryCore
		for (Class<? extends Throwable> type : noRetryCore)
		{
			if (type.isInstance(e))
				return false;
		}

		// By default, retry
		return true;
	}

	@Override
	public String toString()
	{
		return invocation.getMethod().toString();
	}
}
