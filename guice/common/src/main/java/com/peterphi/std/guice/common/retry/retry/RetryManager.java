package com.peterphi.std.guice.common.retry.retry;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.peterphi.std.guice.common.retry.retry.backoff.BackoffStrategy;
import org.apache.log4j.Logger;

public class RetryManager
{
	private static final Logger log = Logger.getLogger(RetryManager.class);

	private final BackoffStrategy backoff;
	private final int maxAttempts;

	private final Timer attempts;
	private final Meter attemptFailures;


	public RetryManager(BackoffStrategy strategy, int maxAttempts, final Timer attempts, final Meter attemptFailures)
	{
		this.attempts = attempts;
		this.attemptFailures = attemptFailures;

		if (strategy == null)
			throw new IllegalArgumentException("Must provide a backoff strategy!");
		if (maxAttempts <= 0)
			throw new IllegalArgumentException("Must provide max attempts!");

		this.backoff = strategy;
		this.maxAttempts = maxAttempts;
	}


	public <T> T run(Retryable<T> operation) throws Exception
	{
		for (int attempt = 1; ; attempt++)
		{
			if (attempt != 1)
				sleep(attempt);

			final Timer.Context timer = attempts.time();

			try
			{
				return operation.attempt(attempt);
			}
			catch (Throwable e)
			{
				attemptFailures.mark();

				final boolean retry = (!maxAttemptsReached(attempt)) && operation.shouldRetry(attempt, e);

				if (!retry)
				{
					return finalAttemptFailed(operation, attempt, e);
				}
				else
				{
					log.warn("Attempt #" + attempt + " of " + operation + " failed, will retry.", e);
				}
			}
			finally
			{
				timer.stop();
			}
		}
	}


	/**
	 * Returns true if the given attempt number is greater than or equal to the maximum number of attempts this retry manager
	 * should
	 * run
	 *
	 * @param attempt
	 *
	 * @return
	 */
	protected boolean maxAttemptsReached(final int attempt)
	{
		return attempt >= maxAttempts;
	}


	/**
	 * Called when the final attempt at a retryable operation failed
	 * <p/>
	 * Allows extending classes to customise behaviour or throw custom exceptions
	 *
	 * @param operation
	 * 		- the operation being attempted
	 * @param attempt
	 * 		- the attempt number that failed
	 * @param e
	 * 		- the exception thrown when the operation failed
	 *
	 * @throws Exception
	 */
	protected <T> T finalAttemptFailed(final Retryable<T> operation, final int attempt, final Throwable e) throws Exception
	{
		log.error("Final attempt #" + attempt + " of " + operation + " failed.", e);

		if (e instanceof Exception)
			throw (Exception) e;
		else if (e instanceof Error)
			throw (Error) e;
		else
			throw new RuntimeException(e);
	}


	/**
	 * Run the operation, only throwing an unchecked exception on failure
	 *
	 * @param operation
	 * @param <T>
	 *
	 * @return
	 *
	 * @throws RuntimeException
	 */
	public <T> T runUnchecked(Retryable<T> operation) throws RuntimeException
	{
		try
		{
			return run(operation);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Retryable " + operation + " failed: " + e.getMessage(), e);
		}
	}


	private void sleep(int attempt)
	{
		final long sleepTime = backoff.getBackoff(attempt);

		try
		{
			Thread.sleep(sleepTime);
		}
		catch (InterruptedException e)
		{
			// ignore
		}
	}
}
