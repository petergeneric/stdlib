package com.peterphi.std.guice.serviceregistry.index;

import java.util.HashSet;
import java.util.Set;

/**
 * A registry of indexable services exposed through this application
 */
public class IndexableServiceRegistry
{
	private static Set<Class<?>> localServices = new HashSet<Class<?>>(4);
	private static Set<ManualIndexableService> remoteServices = new HashSet<ManualIndexableService>(0);
	private static int revision = 0;

	public static void register(Class<?> iface)
	{
		if (!localServices.contains(iface))
		{
			localServices.add(iface);
			revision++;
		}
	}

	public static void register(String serviceInterface, String endpoint)
	{
		register(new ManualIndexableService(serviceInterface, endpoint));
	}

	public static void register(ManualIndexableService service)
	{
		if (!remoteServices.contains(service))
		{
			remoteServices.add(service);
			revision++;
		}
	}

	public static Set<Class<?>> getLocalServices()
	{
		return localServices;
	}

	public static Set<ManualIndexableService> getRemoteServices()
	{
		return remoteServices;
	}

	public static int getRevision()
	{
		return revision;
	}
}
