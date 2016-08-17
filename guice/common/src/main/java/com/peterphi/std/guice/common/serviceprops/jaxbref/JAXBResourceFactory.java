package com.peterphi.std.guice.common.serviceprops.jaxbref;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class JAXBResourceFactory
{
	private final GuiceConfig config;
	private final JAXBSerialiserFactory factory;

	private final Map<String, JAXBNamedResourceFactory> cachedReferences = new HashMap<>();


	@Inject
	public JAXBResourceFactory(GuiceConfig config, JAXBSerialiserFactory factory)
	{
		this.config = config;
		this.factory = factory;
	}


	/**
	 * Resolve the JAXB resource, permitting caching behind the scenes
	 *
	 * @param clazz
	 * 		the jaxb resource to read
	 * @param name
	 * 		the name of the property
	 * @param <T>
	 *
	 * @return
	 */
	public <T> T get(Class<T> clazz, final String name)
	{
		JAXBNamedResourceFactory<T> cached = cachedReferences.get(name);

		if (cached == null)
		{
			cached = new JAXBNamedResourceFactory<T>(this.config, this.factory, name, clazz);
			cachedReferences.put(name, cached);
		}

		return cached.get();
	}


	/**
	 * Resolve the JAXB resource, permitting caching behind the scenes
	 *
	 * @param clazz
	 * 		the jaxb resource to read
	 * @param name
	 * 		the name of the property
	 * @param defaultValue
	 * 		the default value to return if the property is missing
	 * @param <T>
	 *
	 * @return
	 */
	public <T> T get(Class<T> clazz, final String name, T defaultValue)
	{
		JAXBNamedResourceFactory<T> cached = cachedReferences.get(name);

		if (cached == null)
		{
			cached = new JAXBNamedResourceFactory<T>(this.config, this.factory, name, clazz);
			cachedReferences.put(name, cached);
		}

		return cached.get(defaultValue);
	}


	/**
	 * Resolve the JAXB resource once without caching anything
	 *
	 * @param clazz
	 * @param name
	 * @param <T>
	 *
	 * @return
	 */
	public <T> T getOnce(final Class<T> clazz, final String name)
	{
		return new JAXBNamedResourceFactory<T>(this.config, this.factory, name, clazz).get();
	}
}
