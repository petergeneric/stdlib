package com.peterphi.std.util.jaxb;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for JAXBSerialiser instances that can optionally be forced to use EclipseLink MOXy (or use the default JAXBContext
 * implementation acquisition rules).<br />
 * Caches JAXBSerialiser instances created
 */
public class JAXBSerialiserFactory
{
	private final ConcurrentHashMap<Class<?>, JAXBSerialiser> cacheByClass = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, JAXBSerialiser> cacheByContext = new ConcurrentHashMap<>();

	private final boolean useMoxy;


	public JAXBSerialiserFactory(boolean useMoxy)
	{
		this.useMoxy = useMoxy;
	}


	@Deprecated
	public JAXBSerialiserFactory(boolean useMoxy, boolean useSoftReferences)
	{
		this(useMoxy);
	}


	/**
	 * Remove all items from the cache
	 */
	public void clear()
	{
		cacheByContext.clear();
		cacheByClass.clear();
	}


	public JAXBSerialiser getInstance(final Class<?> clazz)
	{
		if (useMoxy)
			return cacheByClass.computeIfAbsent(clazz, JAXBSerialiser :: getMoxy);
		else
			return cacheByClass.computeIfAbsent(clazz, JAXBSerialiser :: getInstance);
	}


	public JAXBSerialiser getInstance(final String contextPath)
	{
		if (useMoxy)
			return cacheByContext.computeIfAbsent(contextPath, JAXBSerialiser :: getMoxy);
		else
			return cacheByContext.computeIfAbsent(contextPath, JAXBSerialiser :: getInstance);
	}
}
