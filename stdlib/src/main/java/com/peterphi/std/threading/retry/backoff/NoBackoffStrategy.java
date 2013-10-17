package com.peterphi.std.threading.retry.backoff;

/**
 * A simple retry backoff that permits a single attempt and then no more
 */
public class NoBackoffStrategy implements BackoffStrategy
{
	@Override
	public long getBackoff(final int attempt)
	{
		return 0;
	}
}
