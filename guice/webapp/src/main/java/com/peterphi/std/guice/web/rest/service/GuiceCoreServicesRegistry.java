package com.peterphi.std.guice.web.rest.service;

import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.serviceregistry.rest.RestResource;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.service.restcore.GuiceCommonRestResources;
import com.peterphi.std.guice.web.rest.service.restcore.GuiceRestCoreService;
import com.peterphi.std.guice.web.rest.service.servicedescription.RestConfigList;
import com.peterphi.std.guice.web.rest.service.servicedescription.RestServiceList;
import org.thymeleaf.util.StringUtils;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiceCoreServicesRegistry
{
	private static class CoreService
	{
		public final String name;
		public final String path;


		public CoreService(final String name, final String path)
		{
			this.name = name;
			this.path = path;
		}
	}


	public List<CoreService> getServices()
	{
		List<CoreService> services = new ArrayList<>();

		for (RestResource resource : RestResourceRegistry.getResources())
		{
			final Class<?> clazz = resource.getResourceClass();

			if (clazz.isAnnotationPresent(Path.class) &&
			    clazz != GuiceCommonRestResources.class &&
			    clazz != GuiceRestCoreService.class)
			{
				final Path path = clazz.getAnnotation(Path.class);

				if (StringUtils.startsWith(path.value(), "/guice"))
				{
					services.add(new CoreService(getName(clazz), path.value()));
				}
			}
		}

		return services;
	}


	private static String getName(final Class<?> clazz)
	{
		if (clazz == RestConfigList.class)
			return "Config";
		else if (clazz == RestServiceList.class)
			return "Services";
		else if (clazz.isAnnotationPresent(ServiceName.class))
			return clazz.getAnnotation(ServiceName.class).value();
		else
			return clazz.getSimpleName();
	}
}