package com.peterphi.std.guice.common.daemon;

import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

public abstract class GuiceRecurringDaemon extends GuiceDaemon
{
	private static final Logger log = Logger.getLogger(GuiceRecurringDaemon.class);

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
			try
			{
				execute();
			}
			catch (Throwable t)
			{
				log.error("Ignoring exception in GuiceRecurringDaemon call", t);
			}

			// Sleep for the default sleep time
			sleep(sleepTime);
		}
	}


	/**
	 * Run one iteration of the daemon
	 *
	 * @throws Exception
	 * 		if an exception occurs (the exception will be logged but otherwise ignored)
	 */
	protected abstract void execute() throws Exception;
}
