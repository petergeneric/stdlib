package com.peterphi.std.guice.serviceregistry.rest;

import com.peterphi.std.guice.serviceregistry.index.IndexableServiceRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds information about the REST resources this webapp exposes
 */
public class RestResourceRegistry
{
	private static Map<Class<?>, RestResource> resources = new HashMap<Class<?>, RestResource>();

	private static int revision = 0;

	/**
	 * Register a new resource - N.B. currently this resource cannot be safely unregistered without restarting the webapp
	 *
	 * @param clazz
	 */
	public static synchronized void register(Class<?> clazz)
	{
		register(clazz, true);
	}

	/**
	 * Register a new resource - N.B. currently this resource cannot be safely unregistered without restarting the webapp
	 *
	 * @param clazz
	 * @param indexable
	 * 		true if this service should also be exposed to any configured index service
	 */
	public static synchronized void register(Class<?> clazz, boolean indexable)
	{
		if (!resources.containsKey(clazz))
		{
			resources.put(clazz, new RestResource(clazz));

			// Optionally register this service as Indexable
			if (indexable)
			{
				IndexableServiceRegistry.register(clazz);
			}

			revision++;
		}
	}

	public static synchronized List<RestResource> getResources()
	{
		return new ArrayList<RestResource>(resources.values());
	}

	public static int getRevision()
	{
		return revision;
	}
}
