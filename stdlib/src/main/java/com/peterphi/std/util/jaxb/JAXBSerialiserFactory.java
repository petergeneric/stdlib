package com.peterphi.std.util.jaxb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A factory for JAXBSerialiser instances that can optionally be forced to use EclipseLink MOXy (or use the default JAXBContext
 * implementation acquisition rules).<br />
 * Caches JAXBSerialiser instances created using either {@link java.lang.ref.SoftReference}s or direct hard references (based on config - defaults to soft references)
 */
public class JAXBSerialiserFactory
{
	private static final Logger log = LoggerFactory.getLogger(JAXBSerialiserFactory.class);

	private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
	private final boolean useMoxy;
	private boolean useSoftReferences;


	public JAXBSerialiserFactory(boolean useMoxy)
	{
		this(useMoxy, true);
	}


	public JAXBSerialiserFactory(boolean useMoxy, boolean useSoftReferences)
	{
		this.useMoxy = useMoxy;
		this.useSoftReferences = useSoftReferences;
	}


	private Object reference(final JAXBSerialiser instance)
	{
		if (instance == null) throw new IllegalArgumentException("Cannot construct null reference for cache!");

		if (useSoftReferences)
			return new SoftReference<>(instance);
		else
			return instance;
	}


	private JAXBSerialiser dereference(final Object obj)
	{
		if (obj == null)
			return null;

		if (useSoftReferences)
			return ((SoftReference<JAXBSerialiser>) obj).get();
		else
			return (JAXBSerialiser) obj;
	}


	protected JAXBSerialiser getInstance(final String key, final Supplier<JAXBSerialiser> provider)
	{
		JAXBSerialiser instance = dereference(cache.get(key));

		if (instance == null)
		{
			log.debug("Cache miss for: " + key);

			instance = provider.get();
			cache.put(key, reference(instance));

			// We just took the penalty to create a JAXBContext, do some maintenance on the map while we're at it
			prune();
		}
		else if (log.isTraceEnabled())
		{
			log.trace("Cache hit for: " + key);
		}

		return instance;
	}


	/**
	 * Remove all items from the cache
	 */
	public void clear()
	{
		cache.clear();
	}


	/**
	 * Finds stale entries in the map
	 */
	private void prune()
	{
		if (useSoftReferences)
		{
			Iterator<Map.Entry<String, Object>> it = cache.entrySet().iterator();

			while (it.hasNext())
			{
				final Map.Entry<String, Object> entry = it.next();

				if (dereference(entry.getValue()) == null)
					it.remove();
			}
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
