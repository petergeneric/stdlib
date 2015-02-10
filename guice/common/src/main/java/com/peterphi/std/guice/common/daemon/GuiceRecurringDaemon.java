package com.peterphi.std.guice.common.daemon;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

public abstract class GuiceRecurringDaemon extends GuiceDaemon
{
	private static final Logger log = Logger.getLogger(GuiceRecurringDaemon.class);

	@Inject
	MetricRegistry metrics;

	private Timer calls;
	private Meter exceptions;

	protected Timeout sleepTime;


	/**
	 * Creates a new Daemon; the Daemon will start once the guice object is fully constructed
	 *
	 * @param sleepTime
	 * 		the amount of time to sleep between calls to the  {@link #execute()} method
	 */
	protected GuiceRecurringDaemon(Timeout sleepTime)
	{
		this(true, sleepTime);
	}


	/**
	 * Creates a new Daemon; the Daemon will start once the guice object is fully constructed
	 *
	 * @param daemonThread
	 * 		true if the thread should be started as a daemon thread, otherwise false
	 * @param sleepTime
	 * 		the amount of time to sleep between calls to the  {@link #execute()} method
	 */
	protected GuiceRecurringDaemon(final boolean daemonThread, final Timeout sleepTime)
	{
		super(daemonThread);

		if (sleepTime == null || sleepTime.getMilliseconds() < 0)
			throw new IllegalArgumentException("Cannot provide a negative sleep time!");
		else
			this.sleepTime = sleepTime;
	}


	@Override
	public void run()
	{
		while (isRunning())
		{
			final Timer.Context timer;

			if (calls != null)
				timer = calls.time();
			else
				timer = null;

			try
			{
				execute();
			}
			catch (Throwable t)
			{
				if (exceptions != null)
					exceptions.mark();

				log.error("Ignoring exception in GuiceRecurringDaemon call", t);
			}
			finally
			{
				if (timer != null)
					timer.stop();
			}

			// Sleep for the default sleep time
			sleep(sleepTime);
		}
	}


	/**
	 * <p>Called when the {@link #execute()} method throws an exception. This method can be overridden by subclasses to record
	 * exception details or to take remedial action. By default it will log the exception at the ERROR level.</p>
	 * <p><b>Note: Any exception thrown by this method will cause the thread to terminate</b></p>
	 *
	 * @param t
	 * 		the exception that was caught
	 */
	protected void executeException(Throwable t)
	{
		log.error("Ignoring exception in GuiceRecurringDaemon call", t);
	}


	/**
	 * Run one iteration of the daemon
	 *
	 * @throws Exception
	 * 		if an exception occurs (the exception will be passed to {@link #executeException(Throwable)} but otherwise ignored)
	 */
	protected abstract void execute() throws Exception;


	@Override
	public void postConstruct()
	{
		this.calls = metrics.timer(GuiceMetricNames.name(getClass(), "calls"));
		this.exceptions = metrics.meter(GuiceMetricNames.name(getClass(), "exceptions"));

		super.postConstruct();
	}
}
