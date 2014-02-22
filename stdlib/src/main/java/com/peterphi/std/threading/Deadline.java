package com.peterphi.std.threading;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents a deadline (some point in the future - generally, a time by which some operation should be completed)
 */
public final class Deadline implements Comparable<Deadline>, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static transient final Logger log = Logger.getLogger(Deadline.class);

	/**
	 * A deadline which will never arrive - representing the year <em>292278994</em>, on <em>Sun Aug 17 at 07:12:55 UTC</em>
	 */
	public static final Deadline MAX_VALUE = new Deadline(Long.MAX_VALUE);

	/**
	 * The current deadline (in milliseconds since 1970 UTC)
	 */
	private final long deadline;


	/**
	 * Constructs a new deadline based on adding <code>timeout</code> to the current time<br />
	 * If <code>current time + timeout > Long.MAX_VALUE</code> then Long.MAX_VALUE is used as the deadline
	 *
	 * @param timeout
	 */
	public Deadline(final Timeout timeout)
	{
		final long now = System.currentTimeMillis();
		final long proposed = now + timeout.getMilliseconds();

		if (proposed >= now)
			this.deadline = proposed;
		else
			this.deadline = Long.MAX_VALUE;

		if (this.deadline < 0)
			throw new IllegalStateException("Deadline has a negative value!");
	}


	/**
	 * @param timestamp
	 */
	public Deadline(final long timestamp)
	{
		if (timestamp < 0)
			throw new IllegalArgumentException("Must provide a positive timestamp for deadline!");

		this.deadline = timestamp;
	}


	/**
	 * Constructs a deadline using a given period of time
	 *
	 * @param timeout
	 * @param unit
	 */
	public Deadline(final long timeout, final TimeUnit unit)
	{
		this(new Timeout(timeout, unit));
	}


	public Deadline(final Calendar calendar)
	{
		this(calendar.getTimeInMillis());
	}


	public Deadline(final Date date)
	{
		this(date.getTime());
	}


	/**
	 * Determines whether the deadline has expired yet
	 *
	 * @return
	 */
	public boolean isExpired()
	{
		return System.currentTimeMillis() >= deadline;
	}


	/**
	 * Determine whether the deadline is still valid (ie. has not expired yet)
	 *
	 * @return
	 */
	public boolean isValid()
	{
		return System.currentTimeMillis() < deadline;
	}


	/**
	 * Determines the amount of time leftuntil the deadline and returns it as a timeout
	 *
	 * @return a timeout representing the amount of time remaining until this deadline expires
	 */
	public Timeout getTimeoutLeft()
	{
		final long left = getTimeLeft();

		if (left != 0)
			return new Timeout(left, TimeUnit.MILLISECONDS);
		else
			return Timeout.ZERO;
	}


	/**
	 * Determines the amount of time left (in milliseconds) until the deadline; if the deadline has been reached or passed this
	 * method returns 0
	 *
	 * @return the number of milliseconds left until this deadline expires
	 */
	public long getTimeLeft()
	{
		final long left = deadline - System.currentTimeMillis();

		if (left > 0)
			return left;
		else
			return 0;
	}


	/**
	 * Returns the time represented by this deadline as a Date
	 *
	 * @return
	 */
	public Date getDate()
	{
		return new Date(this.deadline);
	}


	/**
	 * Determines the amount of time left (in a custom unit) until the deadline; if the deadline has been reached or passed this
	 * method returns 0
	 *
	 * @param unit
	 * 		a unit of time
	 *
	 * @return the amount of time left before this deadline expires, converted (using <code>TimeUnit.convert</code>) into the
	 * given Time Unit
	 */
	public long getTimeLeft(final TimeUnit unit)
	{
		final long left = unit.convert(getTimeLeft(), TimeUnit.MILLISECONDS);

		if (left > 0)
			return left;
		else
			return 0;
	}


	/**
	 * Sleeps the current thread until interrupted or until this deadline has expired. Returns <code>this</code> no matter what
	 * condition causes this method's termination<br />
	 * This is essentially a neater way of calling Thread.sleep
	 *
	 * @return itself
	 */
	public Deadline sleep()
	{
		try
		{
			Thread.sleep(getTimeLeft(TimeUnit.MILLISECONDS));
		}
		catch (InterruptedException e)
		{
			// ignore
		}

		return this;
	}


	/**
	 * Sleeps the current thread until this deadline has expired; if <code>mayInterrupt</code> is true then an interrupt will
	 * cause this method to return true<br />
	 * This is essentially a neater way of calling Thread.sleep
	 *
	 * @param mayInterrupt
	 * 		whether this method may be interrupted by an InterruptedException
	 *
	 * @return whether the sleep was ended early by an interrupt
	 */
	public boolean sleep(final boolean mayInterrupt)
	{
		while (isValid())
		{
			try
			{
				Thread.sleep(getTimeLeft(TimeUnit.MILLISECONDS));
			}
			catch (InterruptedException e)
			{
				if (mayInterrupt)
					return true;
			}
		}

		return false;
	}


	/**
	 * Waits on the listed monitor until it returns or until this deadline has expired; if <code>mayInterrupt</code> is true then
	 * an interrupt will cause this method to return true
	 *
	 * @param ignoreInterrupts
	 * 		false if the code should return early in the event of an interrupt, otherwise true if InterruptedExceptions should be
	 * 		ignored
	 */
	public void waitFor(final Object monitor, final boolean ignoreInterrupts)
	{
		synchronized (monitor)
		{
			try
			{
				monitor.wait(this.getTimeLeft(TimeUnit.MILLISECONDS));
			}
			catch (InterruptedException e)
			{
				if (!ignoreInterrupts)
					return;
			}
		}

		return;
	}


	/**
	 * Resolves a Future, automatically cancelling it in the event of a timeout / failure
	 *
	 * @param <T>
	 * @param future
	 *
	 * @return
	 *
	 * @throws RuntimeException
	 */
	public <T> T resolveFuture(final Future<T> future) throws RuntimeException
	{
		return resolveFuture(future, true);
	}


	/**
	 * Resolves a Future, potentially automatically cancelling it in the event of a timeout / failure
	 *
	 * @param <T>
	 * 		the return type
	 * @param future
	 * @param autocancel
	 * 		if the thread should be cancelled if the deadline expires within this method
	 *
	 * @return the resolved value of the future (or null in the event of a timeout)
	 *
	 * @throws RuntimeException
	 * 		if the operation fails (or is cancelled by another party)
	 */
	public <T> T resolveFuture(final Future<T> future, final boolean autocancel) throws RuntimeException
	{
		while (isValid() && !future.isCancelled())
		{
			try
			{
				return future.get(getTimeLeft(), TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// ignore and start waiting again
			}
			catch (ExecutionException e)
			{
				try
				{
					if (autocancel)
						future.cancel(true);
				}
				catch (Throwable t)
				{
					log.info("{resolveFuture} Error auto-cancelling after ExecutionException: " + t.getMessage(), t);
				}

				throw new RuntimeException(e);
			}
			catch (TimeoutException e)
			{
				break;
			}
		}

		// We timed out
		future.cancel(true);
		return null;
	}


	@Override
	public int compareTo(Deadline that)
	{
		final Long thisDeadline = Long.valueOf(deadline);

		return thisDeadline.compareTo(that.deadline);
	}


	@Override
	public int hashCode()
	{
		// return the hashcode of the Long
		return (int) (deadline ^ (deadline >>> 32));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		else if (o == null)
			return false;
		else if (o.getClass().equals(Deadline.class))
		{
			final Deadline that = (Deadline) o;

			return this.deadline == that.deadline;
		}
		else
		{
			return false;
		}
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Deadline " + new Date(deadline) + ", msLeft=" + this.getTimeLeft() + "]";
	}


	/**
	 * Retrieve the deadline with the least time remaining until it expires
	 *
	 * @param deadlines
	 *
	 * @return
	 */
	public static Deadline soonest(Deadline... deadlines)
	{
		Deadline min = null;

		if (deadlines != null)
			for (Deadline deadline : deadlines)
			{
				if (deadline != null)
				{
					if (min == null)
						min = deadline;
					else if (deadline.getTimeLeft() < min.getTimeLeft())
					{
						min = deadline;
					}
				}
			}

		return min;
	}


	/**
	 * Retrieve the deadline with the most time remaining until it expires
	 *
	 * @param deadlines
	 *
	 * @return
	 */
	public static Deadline latest(Deadline... deadlines)
	{
		Deadline max = null;

		if (deadlines != null)
			for (Deadline deadline : deadlines)
			{
				if (max == null)
					max = deadline;
				else if (deadline.getTimeLeft() > max.getTimeLeft())
				{
					max = deadline;
				}
			}

		return max;
	}
}
