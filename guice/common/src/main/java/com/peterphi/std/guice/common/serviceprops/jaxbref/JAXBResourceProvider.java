package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.google.inject.Provider;
import com.peterphi.std.threading.Timeout;

import java.util.function.Consumer;

public class JAXBResourceProvider<T> implements Provider<T>
{
	private final Provider<JAXBResourceFactory> resourceFactoryProvider;
	private final String propertyName;
	private final Class<T> clazz;
	private final Consumer<T> onLoadMethod;

	private Timeout cacheValidity = Timeout.THIRTY_SECONDS;

	private volatile T value;
	private volatile long cacheExpires = Integer.MIN_VALUE;


	public JAXBResourceProvider(final Provider<JAXBResourceFactory> resourceFactoryProvider,
	                            final String propertyName,
	                            final Class<T> clazz,
	                            final Consumer<T> onLoadMethod)
	{
		this.resourceFactoryProvider = resourceFactoryProvider;
		this.propertyName = propertyName;
		this.clazz = clazz;
		this.onLoadMethod = onLoadMethod;
	}


	/**
	 * Change the period that serialised results will be cached for
	 *
	 * @param cacheValidity
	 */
	public void setCacheValidity(final Timeout cacheValidity)
	{
		this.cacheValidity = cacheValidity;
		this.cacheExpires = Integer.MIN_VALUE;
	}


	@Override
	public synchronized T get()
	{
		final long now = System.currentTimeMillis();

		if (cacheExpires > now)
		{
			final T obj = this.value;

			if (obj != null)
				return obj;
		}

		// Cannot use cache, must recompute
		final T obj = deserialise();

		// If caching is enabled, cache the value
		if (cacheValidity != null)
		{
			this.value = obj;
			this.cacheExpires = now + cacheValidity.getMilliseconds();
		}

		return obj;
	}

	private T deserialise()
	{
		final T obj = resourceFactoryProvider.get().getOnce(clazz, propertyName);

		if (onLoadMethod != null)
			onLoadMethod.accept(obj);

		return obj;
	}
}
