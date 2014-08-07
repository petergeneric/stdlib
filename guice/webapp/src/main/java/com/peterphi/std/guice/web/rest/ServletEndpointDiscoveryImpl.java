package com.peterphi.std.guice.web.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.serviceregistry.ApplicationContextNameRegistry;
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
		if (config.containsKey(LocalEndpointDiscovery.STATIC_ENDPOINT_CONFIG_NAME))
		{
			final String uri = config.getString(LocalEndpointDiscovery.STATIC_ENDPOINT_CONFIG_NAME);

			return URI.create(uri);
		}
		else
		{
			// Need to build up endpoint
			String baseUri = config.getString(LocalEndpointDiscovery.STATIC_CONTAINER_PREFIX_CONFIG_NAME);
			String contextPath;

			if (config.containsKey(LocalEndpointDiscovery.STATIC_CONTEXTPATH_CONFIG_NAME))
			{
				contextPath = config.getString(LocalEndpointDiscovery.STATIC_CONTEXTPATH_CONFIG_NAME);
			}
			else
			{
				// need to figure out webapp name
				contextPath = recoverContextPath();
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


	private String recoverContextPath()
	{
		final String contextPath = ApplicationContextNameRegistry.getContextName();

		if (contextPath != null)
		{
			return contextPath;
		}
		else
		{
			log.warn("Attempt to read webapp name but none has been set yet");
			throw new IllegalStateException("Webapp name has not been set yet!");
		}
	}
}
