package com.peterphi.std.guice.web.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.serviceregistry.LocalEndpointDiscovery;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.net.URI;

@Singleton
public class ServletEndpointDiscoveryImpl implements LocalEndpointDiscovery
{
	private static final Logger log = Logger.getLogger(ServletEndpointDiscoveryImpl.class);

	private final Configuration config;


	@Inject
	public ServletEndpointDiscoveryImpl(Configuration configuration)
	{
		this.config = configuration;
	}


	@Override
	public URI getLocalEndpoint()
	{
		if (config.containsKey(GuiceProperties.STATIC_ENDPOINT_CONFIG_NAME))
		{
			final String uri = config.getString(GuiceProperties.STATIC_ENDPOINT_CONFIG_NAME);

			return URI.create(uri);
		}
		else
		{
			// Need to build up endpoint
			String baseUri = config.getString(GuiceProperties.STATIC_CONTAINER_PREFIX_CONFIG_NAME);
			String contextPath;

			if (config.containsKey(GuiceProperties.STATIC_CONTEXTPATH_CONFIG_NAME))
			{
				contextPath = config.getString(GuiceProperties.STATIC_CONTEXTPATH_CONFIG_NAME);
			}
			else
			{
				// need to figure out webapp name
				contextPath = config.getString(GuiceProperties.SERVLET_CONTEXT_NAME);
			}

			// Try to avoid double-slashing if the baseUri ends with /
			if (baseUri.endsWith("/") && contextPath.startsWith("/"))
			{
				contextPath = contextPath.substring(1); // both have slashes, remove it from the context path
			}
			else if (!baseUri.endsWith("/") && !contextPath.startsWith("/"))
			{
				contextPath = "/" + contextPath; // neither have slashes, add one to the context path
			}

			return URI.create(baseUri + contextPath);
		}
	}
}
