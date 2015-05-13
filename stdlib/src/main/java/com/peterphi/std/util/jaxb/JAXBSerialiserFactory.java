package com.peterphi.std.util.jaxb;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A factory for JAXBSerialiser instances that can optionally be forced to use EclipseLink MOXy (or use the default JAXBContext
 * implementation acquisition rules).<br />
 * Caches JAXBSerialiser instances created using {@link java.lang.ref.SoftReference}s.
 */
public class JAXBSerialiserFactory
{
	private final ConcurrentHashMap<String, SoftReference<JAXBSerialiser>> cache = new ConcurrentHashMap<>();
	private final boolean useMoxy;


	public JAXBSerialiserFactory(boolean useMoxy)
	{
		this.useMoxy = useMoxy;
	}


	protected JAXBSerialiser getInstance(final String key, final Supplier<JAXBSerialiser> provider)
	{
		final SoftReference<JAXBSerialiser> ref = cache.get(key);
		JAXBSerialiser instance = (ref != null) ? ref.get() : null;

		if (instance == null)
		{
			instance = provider.get();
			cache.put(key, new SoftReference<>(instance));

			// We just took the penalty to create a JAXBContext, do some maintenance on the map while we're at it
			prune();
		}

		return instance;
	}


	/**
	 * Finds stale entries in the map
	 */
	private void prune()
	{
		Iterator<Map.Entry<String, SoftReference<JAXBSerialiser>>> it = cache.entrySet().iterator();

		while (it.hasNext())
		{
			final Map.Entry<String, SoftReference<JAXBSerialiser>> entry = it.next();

			if (entry.getValue() == null || entry.getValue().get() == null)
				it.remove();
		}
	}


	public JAXBSerialiser getInstance(final Class<?> clazz)
	{
		return getInstance(clazz.toString(), () -> construct(clazz));
	}


	public JAXBSerialiser getInstance(final String contextPath)
	{
		return getInstance(contextPath, () -> construct(contextPath));
	}


	JAXBSerialiser construct(String contextPath)
	{
		if (useMoxy)
			return JAXBSerialiser.getMoxy(contextPath);
		else
			return JAXBSerialiser.getInstance(contextPath);
	}


	JAXBSerialiser construct(Class<?> clazz)
	{
		if (useMoxy)
			return JAXBSerialiser.getMoxy(clazz);
		else
			return JAXBSerialiser.getInstance(clazz);
	}
}
