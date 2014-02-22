package com.peterphi.std.threading;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple Future whose value will be set at some later date
 *
 * @param <T>
 */
public class SettableFuture<T> implements Future<T>
{
	private static transient final Logger log = Logger.getLogger(SettableFuture.class);

	private T value = null; // Null until it is assigned
	private Throwable exception = null;
	private AtomicInteger state = new AtomicInteger(STATE_UNSET);

	private static final int STATE_UNSET = 0; // Start state. No value is assigned. Next: CANCELLED | SETTING
	private static final int STATE_SETTING = 1; // Short state; value is in the process of being assigned. Next: SET
	private static final int STATE_SET = 3; // A value/exception has been assigned. Terminal state
	private static final int STATE_CANCELLED = 5; // The Future has been cancelled. Terminal State

	private final ParamInvokeable<SettableFuture<T>> onSet;
	private final ParamInvokeable<SettableFuture<T>> onCancel;


	public SettableFuture()
	{
		this(null, null);
	}


	/**
	 * Create a SettableFuture which will potentially execute tasks upon set and upon cancel; the handlers will hold the exclusive
	 * lock on the monitor for the duration of their execution<br />
	 * The handlers, (as they are ParamInvokeables) run synchronously with the call to <code>set</code> or <code>cancel</code>;
	 * they run after the effects have taken place and after any interested parties waiting on the monitor have been notified
	 *
	 * @param onSet
	 * @param onCancel
	 */
	public SettableFuture(ParamInvokeable<SettableFuture<T>> onSet, ParamInvokeable<SettableFuture<T>> onCancel)
	{
		this.onSet = onSet;
		this.onCancel = onCancel;
	}


	public void set(T value)
	{
		if (state.compareAndSet(STATE_UNSET, STATE_SETTING))
		{
			this.value = value;

			state.set(STATE_SET);

			// Terminal state reached. notify everyone.
			synchronized (this)
			{
				this.notifyAll();
			}

			// Run handlers
			on_set();
		}
		else
		{
			throw new IllegalStateException("Cannot set the value twice!");
		}
	}


	public void fail(Throwable exception)
	{
		if (state.compareAndSet(STATE_UNSET, STATE_SETTING))
		{
			this.value = null;
			this.exception = exception;

			state.set(STATE_SET);

			// Terminal state reached. notify everyone.
			synchronized (this)
			{
				this.notifyAll();
			}

			// Run handlers
			on_set();
		}
		else
		{
			throw new IllegalStateException("Cannot set the value twice!");
		}
	}


	protected void on_set()
	{
		if (onSet != null)
			onSet.call(this);
	}


	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return cancel();
	}


	/**
	 * Cancels this Future
	 *
	 * @return
	 */
	public boolean cancel()
	{
		if (!state.compareAndSet(STATE_UNSET, STATE_CANCELLED))
		{

			// Terminal state reached. notify everyone.
			synchronized (this)
			{
				this.notifyAll();
			}

			on_cancel();

			return true;
		}
		else
		{
			return false;
		}
	}


	protected void on_cancel()
	{
		if (onCancel != null)
			onCancel.call(this);
	}


	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		while (true)
		{
			T tmpvalue = null;
			try
			{
				tmpvalue = get(Deadline.MAX_VALUE);

				if (log.isInfoEnabled())
					log.info("[TransferFuture] {get} returning value " + tmpvalue);

				return tmpvalue;
			}
			catch (TimeoutException e)
			{
				// ignore
			}
		}
	}


	public final T poll() throws ExecutionException
	{
		final int state = this.state.get();

		switch (state)
		{
			case STATE_SET:
				if (this.exception != null)
					throw new ExecutionException("Operation threw Exception: " + this.exception.getMessage(), this.exception);
				else
					return this.value;

			case STATE_CANCELLED:
				throw new ExecutionException("Operation cancelled", null);
			default:
				return null;
		}
	}


	@Override
	public final T get(final long quantity, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		final Deadline deadline = new Deadline(quantity, unit);

		return get(deadline);
	}


	public final T get(Deadline deadline) throws InterruptedException, ExecutionException, TimeoutException
	{
		while (deadline.isValid())
		{
			switch (state.get())
			{
				case STATE_SET:
				case STATE_CANCELLED:
					return poll();
				default:
					// Wait for a change
					synchronized (this)
					{
						// Wait no longer than 10 seconds
						final long sleepTime = Math.min(10000, deadline.getTimeLeft());
						this.wait(sleepTime);
					}
			}

		}

		throw new TimeoutException();
	}


	@Override
	public boolean isCancelled()
	{
		return state.get() == STATE_CANCELLED;
	}


	@Override
	public boolean isDone()
	{
		return state.get() == STATE_SET;
	}
}
