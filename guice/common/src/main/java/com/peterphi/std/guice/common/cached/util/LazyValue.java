package com.peterphi.std.guice.common.cached.util;


import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

/**
 * Represents a computation that can be lazily executed. If the supplier returns without exception, the return value will be cached. Thrown exceptions are not cached
 *
 * @param <T>
 */
@ThreadSafe
public class LazyValue<T>
{
	/**
	 * The supplier to invoke. Once called, this will be set to null, which is a signal that the value can be used
	 */
	private volatile Supplier<T> supplier;

	/**
	 * The computed value. May be null. Will be treated as a computed value once <code>supplier == null</code>
	 */
	private volatile T value;


	public LazyValue(@Nonnull final Supplier<T> supplier)
	{
		this(supplier, null);

		if (supplier == null)
			throw new IllegalArgumentException("Must supply non-null supplier!");
	}


	private LazyValue(@Nullable final Supplier<T> supplier, @Nullable T value)
	{
		if (value != null && supplier == null)
			throw new IllegalArgumentException("Must supply no supplier if providing a non-null value!");

		this.supplier = supplier;
		this.value = value; // may be null
	}


	/**
	 * Retrieve or synchronously compute the value.
	 *
	 * @return the computed value (which may be null if the original supplier permitted nulls)
	 * @throws CancellationException if this LazyValue has been cancelled prior to the inner value being computed
	 */
	public synchronized T get()
	{
		if (supplier != null)
			compute();

		return value;
	}


	/**
	 * Cancel the computation if it has not already been computed. Subsequent calls to {@link #get()} will throw a {@link CancellationException}
	 */
	public synchronized void cancel()
	{
		if (supplier != null)
			supplier = () -> {
				throw new CancellationException("Lazy computation cancelled");
			};
	}


	public synchronized boolean isComputed()
	{
		return supplier != null;
	}


	private synchronized void compute()
	{
		if (supplier == null)
			throw new IllegalStateException("compute called with null supplier");

		this.value = supplier.get();
		this.supplier = null;
	}


	/**
	 * Wraps a pre-computed constant as a LazyValue
	 *
	 * @param value value to wrap
	 * @param <T>
	 * @return
	 */
	public static <T> LazyValue<T> of(@Nullable T value)
	{
		return new LazyValue<>(null, value);
	}


	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("supplier", supplier).add("value", value).toString();
	}
}
