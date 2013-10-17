package com.peterphi.std.guice.serviceregistry;

import java.net.URI;

/**
 * An interface to a service which is able to provide the local endpoint
 */
public interface LocalEndpointDiscovery
{
	public static final String STATIC_ENDPOINT_CONFIG_NAME = "local.webapp.endpoint";

	public static final String STATIC_CONTAINER_PREFIX_CONFIG_NAME = "local.container.endpoint";
	public static final String STATIC_CONTEXTPATH_CONFIG_NAME = "local.webapp.context-path";

	/**
	 * Get the endpoint of the local webapp
	 * @return
	 */
	public URI getLocalEndpoint();
}
