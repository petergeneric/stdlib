package com.peterphi.std.guice.common.retry.module;

import com.peterphi.std.guice.common.retry.retry.Retryable;
import com.peterphi.std.guice.restclient.exception.RestException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;

final class InvocationRetryable implements Retryable<Object>
{
	private static final Logger log = Logger.getLogger(InvocationRetryable.class);

	final MethodInvocation invocation;
	final Class<? extends Throwable>[] alwaysRetry;
	final Class<? extends Throwable>[] noRetry;
	final Class<? extends Throwable>[] noRetryCore;
	final int[] noHttpCodes;


	public InvocationRetryable(MethodInvocation invocation,
	                           final Class<? extends Throwable>[] alwaysRetry,
	                           final Class<? extends Throwable>[] noRetry,
	                           final Class<? extends Throwable>[] noRetryCore,
	                           final int[] noHttpCodes)
	{
		this.invocation = invocation;
		this.alwaysRetry = alwaysRetry;
		this.noRetry = noRetry;
		this.noRetryCore = noRetryCore;
		this.noHttpCodes = noHttpCodes;
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

		// Don't retry if a RestExeption/WebApplicationException's Response HTTP Code is in noHttpCodes
		// Also don't retry if a 303 redirect with a cause with an HTTP Code in noHttpCodes
		if (e instanceof RestException || e instanceof WebApplicationException)
		{
			final int[] httpCodes = getHttpCodesForException(e);

			if (httpCodes != null)
			{
				final int httpCode = httpCodes[0];

				if (ArrayUtils.contains(this.noHttpCodes, httpCode))
					return false;

				// See Other (used for login redirects)
				if (httpCode == 303)
				{
					final int causeHttpCode = httpCodes[1];

					if (ArrayUtils.contains(this.noHttpCodes, causeHttpCode))
						return false;
				}
			}
		}

		// By default, retry
		return true;
	}


	private int[] getHttpCodesForException(final Throwable e)
	{
		final int httpCode;
		if (e instanceof RestException)
			httpCode = ((RestException) e).getHttpCode();
		else if (e instanceof WebApplicationException && ((WebApplicationException) e).getResponse() != null)
			httpCode = ((WebApplicationException) e).getResponse().getStatus();
		else
			return null;

		final int causeHttpCode;
		final Throwable cause = e.getCause();
		if (cause == null)
			causeHttpCode = Integer.MIN_VALUE;
		else if (cause instanceof RestException)
			causeHttpCode = ((RestException) cause).getHttpCode();
		else
			causeHttpCode = Integer.MIN_VALUE;

		return new int[]{httpCode, causeHttpCode};
	}


	@Override
	public String toString()
	{
		return invocation.getMethod().toString();
	}
}
