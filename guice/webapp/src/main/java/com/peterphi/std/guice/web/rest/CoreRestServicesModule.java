package com.peterphi.std.guice.web.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.serviceregistry.LocalEndpointDiscovery;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.guice.web.rest.service.restcore.GuiceCommonRestResources;
import com.peterphi.std.guice.web.rest.service.restcore.GuiceRestCoreService;
import com.peterphi.std.guice.web.rest.service.restcore.GuiceRestCoreServiceImpl;
import com.peterphi.std.guice.web.rest.service.servicedescription.RestConfigList;
import com.peterphi.std.guice.web.rest.service.servicedescription.RestServiceList;
import com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerModule;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.net.URI;

public class CoreRestServicesModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(CoreRestServicesModule.class);
	public static final String RESTEASY_MAPPING_PREFIX = "resteasy.servlet.mapping.prefix";


	@Override
	protected void configure()
	{
		// Make freemarker available for REST List service
		install(new FreemarkerModule());

		bind(LocalEndpointDiscovery.class).to(ServletEndpointDiscoveryImpl.class);

		bind(GuiceRestCoreService.class).to(GuiceRestCoreServiceImpl.class).asEagerSingleton();

		RestResourceRegistry.register(GuiceRestCoreService.class);
		RestResourceRegistry.register(GuiceCommonRestResources.class);
		RestResourceRegistry.register(RestServiceList.class);
		RestResourceRegistry.register(RestConfigList.class);
	}


	/**
	 * Retrieves the RESTeasy mapping prefix - this is the path under the webapp root where RESTeasy services are mapped.
	 *
	 * @param context
	 *
	 * @return
	 */
	@Provides
	@Singleton
	@Named("local.restservices.prefix")
	public String getRestServicesPrefix(ServletContext context)
	{
		String restPath = context.getInitParameter(RESTEASY_MAPPING_PREFIX);

		if (restPath == null || restPath.isEmpty() || restPath.equals("/"))
		{
			return "";
		}
		else
		{
			return restPath;
		}
	}


	/**
	 * Return the base path for all REST services in this webapp
	 *
	 * @param webappUri
	 * @param restPrefix
	 * 		the prefix for rest services (added after the webapp endpoint to form the base path for the JAX-RS container)
	 * @param context
	 *
	 * @return
	 */
	@Provides
	@Singleton
	@Named("local.restservices.endpoint")
	public URI getRestServicesEndpoint(@Named("local.webapp.endpoint") URI webappUri,
	                                   @Named("local.restservices.prefix") String restPrefix,
	                                   ServletContext context)
	{
		if (restPrefix.equals(""))
		{
			// resteasy mapped to /
			return webappUri;
		}
		else
		{
			// Strip the leading / from the restpath
			while (restPrefix.startsWith("/"))
				restPrefix = restPrefix.substring(1);

			final String webappPath = webappUri.toString();

			if (webappPath.endsWith("/"))
				return URI.create(webappPath + restPrefix);
			else
				return URI.create(webappPath + "/" + restPrefix);
		}
	}


	/**
	 * Return the base path for this webapp
	 *
	 * @param localEndpointDiscovery
	 *
	 * @return
	 */
	@Provides
	@Singleton
	@Named("local.webapp.endpoint")
	public URI getRestServicesEndpoint(LocalEndpointDiscovery localEndpointDiscovery)
	{
		final URI base = localEndpointDiscovery.getLocalEndpoint();

		return base;
	}
}
