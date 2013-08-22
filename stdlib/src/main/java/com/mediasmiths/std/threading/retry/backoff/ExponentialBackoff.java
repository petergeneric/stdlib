package com.mediasmiths.std.threading.retry.backoff;

import com.mediasmiths.std.threading.Timeout;

public class ExponentialBackoff implements BackoffStrategy
{
	private final long initial;
	private final double exponent;

	private final long maximum;

	public ExponentialBackoff(Timeout initial, double exponent)
	{
		this(initial, exponent, Timeout.MAX_VALUE);
	}

	public ExponentialBackoff(Timeout initial, double exponent, Timeout maximum)
	{
		this.initial = initial.getMilliseconds();
		this.exponent = exponent;
		this.maximum = maximum.getMilliseconds();
	}

	@Override
	public long getBackoff(int attempt)
	{
		final double computed = initial * Math.pow(exponent, attempt - 1);

		return Math.min(Math.round(computed), maximum);
	}
}
