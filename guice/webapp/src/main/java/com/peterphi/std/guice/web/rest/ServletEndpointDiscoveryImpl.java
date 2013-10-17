package com.peterphi.std.guice.web.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.serviceregistry.ApplicationContextNameRegistry;
import com.peterphi.std.guice.serviceregistry.LocalEndpointDiscovery;
import com.peterphi.std.io.PropertyFile;
import org.apache.log4j.Logger;

import java.net.URI;

@Singleton
public class ServletEndpointDiscoveryImpl implements LocalEndpointDiscovery
{
	private static final Logger log = Logger.getLogger(ServletEndpointDiscoveryImpl.class);

	private final PropertyFile serviceProperties;

	@Inject
	public ServletEndpointDiscoveryImpl(@Named("service.properties") PropertyFile serviceProperties)
	{
		this.serviceProperties = serviceProperties;
	}

	@Override
	public URI getLocalEndpoint()
	{
		if (serviceProperties.containsKey(LocalEndpointDiscovery.STATIC_ENDPOINT_CONFIG_NAME))
		{
			final String uri = serviceProperties.get(LocalEndpointDiscovery.STATIC_ENDPOINT_CONFIG_NAME);

			return URI.create(uri);
		}
		else
		{
			// Need to build up endpoint
			String baseUri = serviceProperties.get(LocalEndpointDiscovery.STATIC_CONTAINER_PREFIX_CONFIG_NAME);
			String contextPath;

			if (serviceProperties.containsKey(LocalEndpointDiscovery.STATIC_CONTEXTPATH_CONFIG_NAME))
			{
				contextPath = serviceProperties.get(LocalEndpointDiscovery.STATIC_CONTEXTPATH_CONFIG_NAME);
			}
			else
			{
				// need to figure out webapp namew
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
