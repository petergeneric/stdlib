package com.peterphi.std.util;

import com.peterphi.std.threading.Timeout;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track of how long it has been since a given timestamp (by default, when the instance was created)<br />
 * This is mainly designed for testing timing
 */
public class Ticker implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final long started;


	public Ticker()
	{
		this(System.currentTimeMillis());
	}


	public Ticker(final Date date)
	{
		this(date.getTime());
	}


	public Ticker(final Calendar calendar)
	{
		this(calendar.getTimeInMillis());
	}


	public Ticker(final long started)
	{
		this.started = started;
	}


	@Override
	public final String toString()
	{
		final long elapsed = getElapsed();

		if (elapsed < 1000)
		{
			if (elapsed != 1)
				return elapsed + " milliseconds";
			else
				return "1 millisecond";
		}
		else
		{
			final long seconds = elapsed / 1000;
			final long millis = elapsed % 1000;

			return seconds + "." + millis + " seconds";
		}
	}


	public Date getStartDate()
	{
		return new Date(started);
	}


	public long getStart()
	{
		return this.started;
	}


	public long getElapsed()
	{
		final long now = System.currentTimeMillis();
		final long elapsed = now - started;

		return elapsed;
	}


	public Timeout getElapsedTime()
	{
		return new Timeout(getElapsed(), TimeUnit.MILLISECONDS);
	}
}
