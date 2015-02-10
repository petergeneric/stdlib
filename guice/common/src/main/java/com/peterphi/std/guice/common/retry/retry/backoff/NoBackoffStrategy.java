package com.peterphi.std.guice.common.retry.retry.backoff;

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
