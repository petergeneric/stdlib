package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.ConfigRef;
import com.peterphi.std.threading.Timeout;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class JAXBResourceProvider<T> implements Provider<T>
{
	private final Provider<JAXBResourceFactory> resourceFactoryProvider;
	private final Provider<ConfigRef> resource;
	private final Class<T> clazz;
	private final Consumer<T> onLoadMethod;

	private Timeout cacheValidity = new Timeout(30, TimeUnit.SECONDS);

	private volatile T value;
	private volatile long cacheExpires = Integer.MIN_VALUE;

	private final Object lock = new Object();


	public JAXBResourceProvider(final Provider<JAXBResourceFactory> resourceFactoryProvider,
	                            final Provider<ConfigRef> resource,
	                            final Class<T> clazz,
	                            final Consumer<T> onLoadMethod)
	{
		this.resourceFactoryProvider = resourceFactoryProvider;
		this.resource = resource;
		this.clazz = clazz;
		this.onLoadMethod = onLoadMethod;
	}


	/**
	 * Change the period that serialised results will be cached for
	 *
	 * @param cacheValidity
	 */
	public JAXBResourceProvider<T> withCacheValidity(final Timeout cacheValidity)
	{
		this.cacheValidity = cacheValidity;
		this.cacheExpires = Integer.MIN_VALUE;

		return this;
	}


	@Override
	public T get()
	{
		if (cacheExpires > System.currentTimeMillis())
		{
			final T obj = this.value;

			if (obj != null)
				return obj;
			else
				return compute();
		}
		else
		{
			return compute();
		}
	}


	/**
	 * Synchronized to avoid multiple concurrent deserialisations (not a correctness issue, but could be a perf issue)
	 *
	 * @return
	 */
	@Nonnull
	private synchronized T compute()
	{
		if (cacheExpires <= System.currentTimeMillis() || this.value == null)
		{
			final T obj = deserialise();

			// If caching is enabled, cache the value
			if (cacheValidity != null)
			{
				this.value = obj;
				this.cacheExpires = System.currentTimeMillis() + cacheValidity.getMilliseconds();
			}

			return obj;
		}
		else
		{
			return this.value;
		}
	}


	private T deserialise()
	{
		final T obj = resourceFactoryProvider.get().getOnce(clazz, resource.get().getName());

		if (onLoadMethod != null)
			onLoadMethod.accept(obj);

		return obj;
	}
}
