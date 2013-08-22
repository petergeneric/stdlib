package com.mediasmiths.std.util.jaxb;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for JAXBSerialiser instances that can optionally be forced to use EclipseLink MOXy (or use the default JAXBContext implementation acquisition rules)
 */
public class JAXBSerialiserFactory
{
	private final ConcurrentHashMap<String, JAXBSerialiser> cache = new ConcurrentHashMap<String, JAXBSerialiser>();
	private final boolean useMoxy;

	public JAXBSerialiserFactory(boolean useMoxy)
	{
		this.useMoxy = useMoxy;
	}

	public JAXBSerialiser getInstance(Class<?> clazz)
	{
		final String str = clazz.toString();

		JAXBSerialiser instance = cache.get(str);

		if (instance == null)
		{
			instance = construct(clazz);
			cache.put(str, instance);
		}

		return instance;
	}

	public JAXBSerialiser getInstance(String contextPath)
	{
		JAXBSerialiser instance = cache.get(contextPath);

		if (instance == null)
		{
			instance = construct(contextPath);
			cache.put(contextPath, instance);
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
