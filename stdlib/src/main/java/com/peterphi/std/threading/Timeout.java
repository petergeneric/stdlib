package com.peterphi.std.threading;

import org.joda.time.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Represents an immutable Timeout (ie. a duration). Timeouts are distinct from Deadlines in that they do not have a start
 * time.<br />
 * This class can be used to represent a duration without needing a dependency on Joda time
 */
public final class Timeout implements Comparable<Timeout>, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A zero-length timeout
	 */
	public static final Timeout ZERO = new Timeout(0, TimeUnit.MILLISECONDS);

	/**
	 * 0.25 seconds (250 milliseconds)
	 */
	public static final Timeout ONE_QUARTER_SECOND = new Timeout(250, TimeUnit.MILLISECONDS);
	/**
	 * One second
	 */
	public static final Timeout ONE_SECOND = new Timeout(1, TimeUnit.SECONDS);
	/**
	 * Ten seconds
	 */
	public static final Timeout TEN_SECONDS = new Timeout(10, TimeUnit.SECONDS);
	/**
	 * Thirty seconds
	 */
	public static final Timeout THIRTY_SECONDS = new Timeout(30, TimeUnit.SECONDS);
	/**
	 * One minute
	 */
	public static final Timeout ONE_MINUTE = new Timeout(1, TimeUnit.MINUTES);
	/**
	 * Five minutes
	 */
	public static final Timeout FIVE_MINUTES = new Timeout(5, TimeUnit.MINUTES);
	/**
	 * 30 minutes
	 */
	public static final Timeout THIRTY_MINUTES = new Timeout(30, TimeUnit.MINUTES);

	/**
	 * The maximum possible timeout
	 */
	public static final Timeout MAX_VALUE = new Timeout(Long.MAX_VALUE);

	private final long period;
	private final TimeUnit unit;


	public Timeout(Duration duration)
	{
		this(duration.getMillis());
	}


	/**
	 * Constructs a deadline using a given period of time
	 *
	 * @param milliseconds
	 * 		period in milliseconds
	 */
	public Timeout(final long milliseconds)
	{
		this(milliseconds, TimeUnit.MILLISECONDS);
	}


	/**
	 * Constructs a deadline using a given period of time
	 *
	 * @param period
	 * 		the number of units
	 * @param unit
	 * 		the unit of time being used
	 */
	public Timeout(final long period, final TimeUnit unit)
	{
		if (unit == null)
			throw new IllegalArgumentException("Must supply a unit");
		if (period < 0)
			throw new IllegalArgumentException("Timeouts must be positive!");

		this.period = period;
		this.unit = unit;
	}


	/**
	 * Create a new deadline which is the given period of time in the future
	 *
	 * @return a deadline the given period of time in the future
	 */
	public Deadline start()
	{
		return new Deadline(this);
	}


	/**
	 * Create a new deadline within a given maximum timeframe; this prevents
	 *
	 * @param max
	 * 		the maximum deadline
	 *
	 * @return a new deadline which is at most the provided maximum
	 */
	public Deadline start(final Deadline max)
	{
		return Deadline.soonest(max, start());
	}


	/**
	 * Get the number of milliseconds this timeout represents
	 *
	 * @return
	 */
	public long getMilliseconds()
	{
		return unit.toMillis(period);
	}


	/**
	 * Get this timeout represented in a different unit
	 *
	 * @param toUnit
	 *
	 * @return
	 */
	public long get(final TimeUnit toUnit)
	{
		if (this.unit == toUnit)
			return period;
		else
			return toUnit.convert(period, this.unit);
	}


	@Override
	public int hashCode()
	{
		return (int) (period ^ (period >>> 32)) ^ unit.hashCode();
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		else if (o == null)
			return false;
		else if (o.getClass().equals(Timeout.class))
		{
			final Timeout that = (Timeout) o;

			return this.getMilliseconds() == that.getMilliseconds();
		}
		else
		{
			return false;
		}
	}


	/**
	 * Create a new Timeout which is equal to this timeout multiplied by some value
	 *
	 * @param by
	 * 		the amount to multiply the current timeout by
	 *
	 * @return a new Timeout which is equal to this timeout multiplied by some value
	 */
	public Timeout multiply(final long by)
	{
		return new Timeout(period * by, this.unit);
	}


	/**
	 * Create a new Timeout which is equal to this timeout multiplied by some value<br />
	 * The resulting Timeout will be in <em>milliseconds</em> to maximise precision.
	 *
	 * @param by
	 * 		the amount to multiply the current timeout by
	 *
	 * @return a new Timeout which is equal to this timeout multiplied by some value (with the unit now set to Milliseconds)
	 */
	public Timeout multiply(final double by)
	{
		final double newMillis = getMilliseconds() * by;
		final long rounded = Math.round(newMillis);

		return new Timeout(rounded, TimeUnit.MILLISECONDS);
	}


	@Override
	public String toString()
	{
		return "[Timeout: " + toEnglishString() + "]";
	}


	public String toEnglishString()
	{
		return Long.toString(period) + " " + unit.toString().toLowerCase();
	}


	@Override
	public int compareTo(final Timeout that)
	{
		final Long thisPeriod = new Long(this.period);
		final long thatPeriod = that.get(this.unit);

		return thisPeriod.compareTo(thatPeriod);
	}


	/**
	 * Sleep for the duration of this timeout (or until the thread is interrupted)
	 */
	public void sleep()
	{
		try
		{
			Thread.sleep(getMilliseconds());
		}
		catch (InterruptedException e)
		{
			// ignore but return
		}
	}


	/**
	 * Sleep for the duration of this timeout (or until the thread is interrupted, or until the provided deadline expires)
	 */
	public void sleep(Deadline deadline)
	{
		final long millis = Math.min(deadline.getTimeLeft(), getMilliseconds());

		if (millis > 0)
		{
			try
			{
				Thread.sleep(millis);
			}
			catch (InterruptedException e)
			{
				// ignore but return
			}
		}
	}


	public Duration toDuration()
	{
		return new Duration(getMilliseconds());
	}


	/**
	 * Filter through a number of timeouts to find the one with the longest period
	 *
	 * @param timeouts
	 * 		a list of timeouts (nulls are ignored)
	 *
	 * @return the timeout with the longest period
	 *
	 * @throws IllegalArgumentException
	 * 		if no non-null timeouts are presented
	 */
	public static Timeout max(Timeout... timeouts)
	{
		Timeout max = null;

		for (Timeout timeout : timeouts)
		{
			if (timeout != null)
				if (max == null || max.getMilliseconds() < timeout.getMilliseconds())
					max = timeout;
		}

		if (max != null)
			return max;
		else
			throw new IllegalArgumentException("Must specify at least one non-null timeout!");
	}


	/**
	 * Filter through a number of timeouts to find the one with the shortest period
	 *
	 * @param timeouts
	 * 		a list of timeouts (nulls are ignored)
	 *
	 * @return the timeout with the shortest period
	 *
	 * @throws IllegalArgumentException
	 * 		if no non-null timeouts are presented
	 */
	public static Timeout min(Timeout... timeouts)
	{
		Timeout min = null;

		for (Timeout timeout : timeouts)
		{
			if (timeout != null)
				if (min == null || min.getMilliseconds() > timeout.getMilliseconds())
					min = timeout;
		}

		if (min != null)
			return min;
		else
			throw new IllegalArgumentException("Must specify at least one non-null timeout!");
	}


	/**
	 * Adds together all the supplied timeouts
	 *
	 * @param timeouts
	 * 		a list of timeouts (nulls are ignored)
	 *
	 * @return the sum of all the timeouts specified
	 */
	public static Timeout sum(Timeout... timeouts)
	{
		long sum = 0;

		for (Timeout timeout : timeouts)
			if (timeout != null)
				sum += timeout.getMilliseconds();

		if (sum != 0)
			return new Timeout(sum);
		else
			return Timeout.ZERO;
	}
}
