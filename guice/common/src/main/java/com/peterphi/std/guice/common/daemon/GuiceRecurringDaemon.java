package com.peterphi.std.guice.common.daemon;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.logging.LoggingMDCConstants;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.stringparsing.StringToTypeConverter;
import com.peterphi.std.threading.Timeout;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.time.Instant;

public abstract class GuiceRecurringDaemon extends GuiceDaemon
{
	private static final Logger log = Logger.getLogger(GuiceRecurringDaemon.class);

	@Inject
	MetricRegistry metrics;

	private Timer calls;
	private Meter exceptions;

	protected Timeout sleepTime;

	private Instant lastRan = null;

	/**
	 * True while the user {@link #execute()} method is running
	 */
	private volatile boolean userCodeRunning = false;


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


	public boolean isUserCodeRunning()
	{
		return this.userCodeRunning;
	}


	@Inject
	public void setSleepTimeFromConfigIfSet(GuiceConfig config)
	{
		final String str = config.get("daemon." + getName() + ".interval", null);

		if (StringUtils.isNotEmpty(str))
		{
			final Timeout timeout = (Timeout) StringToTypeConverter.convert(Timeout.class, str);

			if (timeout == null)
				throw new IllegalArgumentException("Invalid interval config: " + str);
			else
				setSleepTime(timeout);
		}
	}


	public Timeout getSleepTime()
	{
		return sleepTime;
	}


	public Timer getCalls()
	{
		return calls;
	}


	public Meter getExceptions()
	{
		return exceptions;
	}


	public void setSleepTime(final Timeout sleepTime)
	{
		this.sleepTime = sleepTime;
	}


	@Override
	public void run()
	{
		while (isRunning())
		{
			lastRan = Instant.now();

			final Timer.Context timer;

			if (calls != null)
				timer = calls.time();
			else
				timer = null;

			try
			{
				// Assign a trace id for operations performed by this run of this thread
				MDC.put(LoggingMDCConstants.TRACE_ID, getThreadName() + "@" + lastRan.toString());

				userCodeRunning = true;
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
				userCodeRunning = false;
				MDC.clear();

				if (timer != null)
					timer.stop();
			}

			// Sleep for the default sleep time
			if (isRunning())
			{
				sleep(sleepTime);
			}
		}
	}


	public Instant getLastRan()
	{
		return this.lastRan;
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


	/**
	 * Trigger the user code to run ASAP
	 *
	 * @throws IllegalStateException
	 * 		if the user code is already running or if the daemon is stopped
	 */
	public void trigger()
	{
		if (userCodeRunning)
		{
			throw new IllegalStateException("User code already running!");
		}
		else if (!isRunning())
		{
			throw new IllegalStateException("Daemon is in the process of terminating!");
		}
		else
		{
			synchronized (this)
			{
				this.notifyAll();
			}
		}
	}
}
