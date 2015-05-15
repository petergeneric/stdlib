package com.peterphi.std.threading;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Simplifies the creation of long-running daemon threads
 */
public abstract class Daemon implements Runnable
{
	private static final Logger log = Logger.getLogger(Daemon.class);

	/**
	 * The thread this daemon is running in
	 */
	private transient Thread thisThread = null;

	/**
	 * False if the daemon has received a request to terminate. True if the thread is running without a termination request
	 */
	private volatile boolean running = false;


	/**
	 * Starts this daemon, creating a new thread for it (the name of which will be set to the SimpleName of its class)
	 *
	 * @return Thread The daemon's thread
	 *
	 * @throws IllegalThreadStateException
	 * 		If the daemon is still running
	 */
	public synchronized Thread startThread() throws IllegalThreadStateException
	{
		return startThread(getThreadName());
	}


	/**
	 * Return the name for this thread
	 * @return
	 */
	protected String getThreadName()
	{
		return this.getClass().getSimpleName();
	}


	/**
	 * Starts this daemon, creating a new thread for it
	 *
	 * @param name
	 * 		String The name for the thread
	 *
	 * @return Thread The daemon's thread
	 *
	 * @throws IllegalThreadStateException
	 * 		If the daemon is still running
	 */
	public synchronized Thread startThread(String name) throws IllegalThreadStateException
	{
		if (!running)
		{
			log.info("[Daemon] {startThread} Starting thread " + name);
			this.running = true;
			thisThread = new Thread(this, name);
			thisThread.setDaemon(shouldStartAsDaemon()); // Set whether we're a daemon thread (false by default)
			thisThread.start();
			return thisThread;
		}
		else
		{
			throw new IllegalThreadStateException("Daemon must be stopped before it may be started");
		}
	}


	/**
	 * Returns whether the daemon is in the process of terminating
	 *
	 * @return boolean True if the daemon is terminated or terminating, otherwise false
	 */
	public synchronized boolean isRunning()
	{
		return running;
	}


	/**
	 * Requests the daemon terminate by setting a flag and sending an interrupt to the thread
	 */
	public synchronized void stopThread()
	{
		if (isRunning())
		{
			if (log.isInfoEnabled())
				log.info("[Daemon] {stopThread} Requesting termination of thread " + thisThread.getName());

			this.running = false;

			synchronized (this)
			{
				this.notifyAll();
			}
		}
		else
		{
			throw new IllegalThreadStateException("Daemon must be started before it may be stopped.");
		}
	}


	/**
	 * Determines if this daemon's thread is alive
	 *
	 * @return boolean True if the thread is alive, otherwise false
	 */
	public synchronized boolean isThreadRunning()
	{
		if (thisThread != null)
			return thisThread.isAlive();
		else
			return false;
	}


	/**
	 * Overloading this method to return true will start this daemon's thread as a Daemon Thread
	 *
	 * @return boolean True if the thread should be started as a daemon, otherwise false
	 */
	protected boolean shouldStartAsDaemon()
	{
		return false;
	}


	// /////////////////////////////////////
	// ////// STATIC HELPER METHODS
	// //////////////////////////////////


	/**
	 * Stop all the daemons in a given list:
	 *
	 * @param daemons
	 * 		List[Daemon] The daemons to stop
	 */
	public static void stopAll(List<? extends Daemon> daemons)
	{
		for (Daemon d : daemons)
		{
			if (d.isRunning())
				d.stopThread();
		}
	}


	public static boolean waitForTermination(Daemon daemon, long timeoutMillis)
	{
		List<Daemon> v = Collections.singletonList(daemon);

		return waitForTermination(v, timeoutMillis);
	}


	public static boolean waitForTermination(List<? extends Daemon> daemons, long timeoutMillis)
	{
		long timeout = (timeoutMillis <= 0) ? Long.MAX_VALUE : (System.currentTimeMillis() + timeoutMillis);

		int size = daemons.size();
		int terminated = 0;
		List<Daemon> terminatedDaemons = new Vector<Daemon>(daemons.size());

		Daemon.stopAll(daemons);

		while (System.currentTimeMillis() < timeout && terminated != size)
		{ // Loop until all daemons are terminated or we timeout
			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				if (timeoutMillis == 0)
				{
					return false;
				}
			}

			// Update the list of terminated daemons:
			for (Daemon d : daemons)
			{
				if (!terminatedDaemons.contains(d))
				{
					if (!d.isThreadRunning())
					{
						terminatedDaemons.add(d);
						terminated++;
					}
				}
			}
		}

		return (terminated == size);
	}
}
