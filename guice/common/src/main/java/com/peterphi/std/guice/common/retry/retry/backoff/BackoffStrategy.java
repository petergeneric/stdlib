package com.peterphi.std.guice.common.retry.retry.backoff;

public interface BackoffStrategy
{
	/**
	 * Get the time to wait after the <code>attempt</code><sup>th</sup> failure
	 *
	 * @param attempt
	 * 		the attempt number (starting at 1)
	 *
	 * @return
	 */
	public long getBackoff(int attempt);
}
