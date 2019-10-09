package com.peterphi.std.guice.common.daemon;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.breaker.BreakerService;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.stringparsing.StringToTypeConverter;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.util.tracing.Tracing;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.time.Instant;

public abstract class GuiceRecurringDaemon extends GuiceDaemon
{
	private static final Logger log = Logger.getLogger(GuiceRecurringDaemon.class);

	@Inject
	MetricRegistry metrics;

	@Inject
	BreakerService breakerService;

	private Timer calls;
	private Meter exceptions;

	protected Timeout sleepTime;

	private Instant lastRan = null;

	/**
	 * True while the user {@link #execute()} method is running
	 */
	private volatile boolean userCodeRunning = false;
	private volatile boolean nextRunVerbose = false;


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


	public Timeout getInitialSleepTime()
	{
		return Timeout.ZERO;
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


	/**
	 * Enable Verbose Tracing for the next run of this daemon
	 */
	public void makeNextRunVerbose()
	{
		this.nextRunVerbose = true;
	}

	@Override
	public void run()
	{
		// Optionally sleep for some time before the first run
		sleep(getInitialSleepTime());

		while (isRunning())
		{
			if (shouldContinue())
			{
				lastRan = Instant.now();

				final Timer.Context timer;

				if (calls != null)
					timer = calls.time();
				else
					timer = null;

				final String traceId = getThreadName() + "@" + lastRan.getEpochSecond();

				try
				{
					// Assign a trace id for operations performed by this run of this thread
					Tracing.start(traceId, nextRunVerbose);

					if (nextRunVerbose)
						nextRunVerbose = false;

					userCodeRunning = true;

					setTextState("");

					execute();
				}
				catch (Throwable t)
				{
					if (exceptions != null)
						exceptions.mark();

					executeException(t);
				}
				finally
				{
					userCodeRunning = false;

					Tracing.stop(traceId);

					if (timer != null)
						timer.stop();
				}
			}
			else
			{
				setTextState("Breaker tripped, will not execute user code until breaker is reset...");
			}

			// Sleep for the default sleep time
			if (isRunning())
			{
				sleep(getSleepTime());
			}
		}
	}


	/**
	 * Returns true if the execution of user code should proceed; ordinarily this will return true.
	 *
	 * If this method returns false then:
	 * <ul>
	 *     <li>If in user code, the user code SHOULD attempt to return control as soon as possible, without leaving any work undone that would not be completed on the next invocation of the user code</li>
	 *     <li>If not in user code, the control logic WILL ensure that user code is not invoked; {@link #isRunning()} will also be tested to determine if the daemon should shutdown</li>
	 * </ul>
	 *
	 * @return
	 */
	public boolean shouldContinue()
	{
		return isRunning() && getBreaker().isNormal();
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
		else if (!shouldContinue())
		{
			throw new IllegalStateException("Breaker has been tripped, user code will not be invoked until it is reset");
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
