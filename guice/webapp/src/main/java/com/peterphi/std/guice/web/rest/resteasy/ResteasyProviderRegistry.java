package com.peterphi.std.guice.web.rest.resteasy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds information about the REST providers this webapp uses
 */
public class ResteasyProviderRegistry
{
	private static Set<Class<?>> providers = new HashSet<Class<?>>();

	private static int revision = 0;


	/**
	 * Register a provider class
	 *
	 * @param clazz
	 */
	public static synchronized void register(Class<?> clazz)
	{
		if (clazz.isAnnotationPresent(javax.ws.rs.ext.Provider.class))
		{
			providers.add(clazz);
			revision++;
		}
		else
		{
			throw new RuntimeException("Class "+clazz.getName()+ " is not annotated with javax.ws.rs.ext.Provider");
		}
	}


	/**
	 * List the registered providers
	 *
	 * @return
	 */
	public static synchronized List<Class<?>> getProviders()
	{
		return new ArrayList<Class<?>>(providers);
	}
}
