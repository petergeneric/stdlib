package com.peterphi.std.guice.common.retry.retry;

/**
 *
 */
public interface Retryable<T>
{
	public T attempt(int attempt) throws Exception;

	/**
	 * Decides whether a retry should be attempted after the provided exception occurs
	 *
	 * @param attempt
	 * 		the attempt number
	 * @param e
	 * 		the thrown exception
	 *
	 * @return true if a further attempt should be made after the listed exception
	 */
	public boolean shouldRetry(int attempt, Throwable e);
}
