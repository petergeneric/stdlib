package com.peterphi.std.util.jaxb;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

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


	public JAXBSerialiser getInstance(Class<?> clazz)
	{
		final String str = clazz.toString();

		final SoftReference<JAXBSerialiser> ref = cache.get(str);
		JAXBSerialiser instance = (ref != null) ? ref.get() : null;

		if (instance == null)
		{
			instance = construct(clazz);
			cache.put(str, new SoftReference<>(instance));
		}

		return instance;
	}


	public JAXBSerialiser getInstance(String contextPath)
	{
		final SoftReference<JAXBSerialiser> ref = cache.get(contextPath);
		JAXBSerialiser instance = (ref != null) ? ref.get() : null;

		if (instance == null)
		{
			instance = construct(contextPath);
			cache.put(contextPath, new SoftReference<>(instance));
		}

		return instance;
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
