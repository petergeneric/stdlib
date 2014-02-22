package com.peterphi.std.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper type which takes a Callable and turns it into a RunnableFuture
 *
 * @param <T>
 */
public class RunnableCallableFuture<T> extends SettableFuture<T> implements Runnable, RunnableFuture<T>
{
	/**
	 * The callable to execute
	 */
	private final Callable<T> callable;

	/**
	 * Set to true once run() has been called
	 */
	private final AtomicBoolean started = new AtomicBoolean(false);


	public RunnableCallableFuture(Callable<T> callable)
	{
		if (callable == null)
			throw new IllegalArgumentException("Must supply a non-null Callable!");

		this.callable = callable;
	}


	@Override
	public void run()
	{
		if (started.compareAndSet(false, true))
		{
			final T value;
			try
			{
				value = callable.call();
			}
			catch (Throwable e)
			{
				fail(e);
				return;
			}

			set(value);
		}
	}


	/**
	 * Start a new daemon thread to call the run() method asynchronously, returning this object as a Future (and not a
	 * RunnableCallableFuture)
	 *
	 * @return
	 */
	public Future<T> asyncRun()
	{
		final Thread t = new Thread(this);
		{
			t.setName("AsyncRun for " + this);
			t.setDaemon(true);
			t.start();
		}

		return this;
	}


	/**
	 * Runs this Callable asynchronously using the specified {@link Executor}
	 *
	 * @param executor
	 *
	 * @return
	 */
	public Future<T> asyncRun(final Executor executor)
	{
		executor.execute(this);

		return this;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[RunnableCallableFuture callable=" + callable + "]";
	}


	/**
	 * Takes a Callable, executing it in the background, returning a Future to its result<br />
	 * Users are advised to use a ThreadPool instead of this method.
	 *
	 * @param <T>
	 * 		the return type
	 * @param callable
	 * 		the callable to return a Future to
	 *
	 * @return
	 */
	public static <T> Future<T> asyncRun(Callable<T> callable)
	{
		RunnableCallableFuture<T> future = new RunnableCallableFuture<T>(callable);

		future.asyncRun();

		return future;
	}
}
