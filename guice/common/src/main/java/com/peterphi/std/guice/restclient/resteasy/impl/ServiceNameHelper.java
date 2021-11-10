package com.peterphi.std.guice.restclient.resteasy.impl;

import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.apploader.GuiceServiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.Objects;

public class ServiceNameHelper
{
	/**
	 * Get the name under which a given service interface is known
	 * @param config
	 * @param iface the service interface; if unspecified then
	 * @param names optional (may be null or empty) set of names to try
	 * @return
	 */
	public static String getName(final GuiceConfig config, Class<?> iface, String... names)
	{
		if (names == null || names.length == 0)
		{
			if (iface == null)
				throw new IllegalArgumentException("If not specifying service names you must provide a service interface");
			else
				names = getServiceNames(iface);
		}

		for (String name : names)
		{
			if (name == null)
				continue;

			if (config.containsKey(GuiceServiceProperties.prop(GuiceServiceProperties.ENDPOINT, name)))
				return name;
		}

		return null;
	}


	/**
	 * Computes the default set of names for a service based on an interface class. The names produced are an ordered list:
	 * <ul>
	 * <li>The fully qualified class name</li>
	 * <li>If present, the {@link com.peterphi.std.annotation.ServiceName} annotation on the class (OR if not specified on the
	 * class, the {@link com.peterphi.std.annotation.ServiceName} specified on the package)</li>
	 * <li>The simple name of the class (the class name without the package prefix)</li>
	 * </ul>
	 *
	 * @param iface a JAX-RS service interface
	 * @return An array containing one or more names that could be used for the class; may contain nulls (which should be ignored)
	 */
	public static String[] getServiceNames(Class<?> iface)
	{
		Objects.requireNonNull(iface, "Missing param: iface!");

		return new String[]{iface.getName(), getServiceName(iface), iface.getSimpleName()};
	}


	private static String getServiceName(Class<?> iface)
	{
		Objects.requireNonNull(iface, "Missing param: iface!");

		if (iface.isAnnotationPresent(ServiceName.class))
		{
			return iface.getAnnotation(ServiceName.class).value();
		}
		else if (iface.getPackage().isAnnotationPresent(ServiceName.class))
		{
			return iface.getPackage().getAnnotation(ServiceName.class).value();
		}
		else
		{
			return null; // No special name
		}
	}
}
