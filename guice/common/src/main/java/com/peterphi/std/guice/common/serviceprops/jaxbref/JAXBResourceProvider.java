package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.google.inject.Provider;
import com.peterphi.std.guice.common.serviceprops.ConfigRef;
import com.peterphi.std.threading.Timeout;

import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;

public class JAXBResourceProvider<T> implements Provider<T>
{
	private final Provider<JAXBResourceFactory> resourceFactoryProvider;
	private final Provider<ConfigRef> resource;
	private final Class<T> clazz;
	private Timeout cacheValidity = new Timeout(30, TimeUnit.SECONDS);

	private volatile SoftReference<T> cachedValue = new SoftReference<T>(null);
	private volatile long cacheExpires = Integer.MIN_VALUE;


	public JAXBResourceProvider(final Provider<JAXBResourceFactory> resourceFactoryProvider,
	                            final Provider<ConfigRef> resource,
	                            final Class<T> clazz)
	{
		this.resourceFactoryProvider = resourceFactoryProvider;
		this.resource = resource;
		this.clazz = clazz;
	}


	/**
	 * Change the period that serialised results will be cached for
	 *
	 * @param cacheValidity
	 */
	public JAXBResourceProvider withCacheValidity(final Timeout cacheValidity)
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
			final T obj = cachedValue.get();

			if (obj != null)
				return obj;
		}

		final T obj = deserialise();

		// If caching is enabled, cache the value
		if (cacheValidity != null)
		{
			cachedValue = new SoftReference<>(obj);
			cacheExpires = System.currentTimeMillis() + cacheValidity.getMilliseconds();
		}

		return obj;
	}


	private T deserialise()
	{
		return resourceFactoryProvider.get().getOnce(clazz, resource.get().getName());
	}
}
