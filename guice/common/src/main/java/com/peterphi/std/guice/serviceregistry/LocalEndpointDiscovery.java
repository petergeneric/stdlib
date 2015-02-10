package com.peterphi.std.guice.serviceregistry;

import java.net.URI;

/**
 * An interface to a service which is able to provide the local endpoint
 */
public interface LocalEndpointDiscovery
{
	/**
	 * Get the endpoint of the local webapp
	 *
	 * @return
	 */
	public URI getLocalEndpoint();
}
