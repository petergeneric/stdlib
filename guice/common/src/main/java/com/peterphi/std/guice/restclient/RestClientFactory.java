package com.peterphi.std.guice.restclient;

import com.google.inject.ImplementedBy;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyClientFactoryImpl;

/**
 * A factory that builds dynamic proxy clients (using JAX-RS RESTful client interfaces) for services, abstracting away the method
 * through which client endpoints, credentials, etc. are acquired
 */
@ImplementedBy(ResteasyClientFactoryImpl.class)
public interface RestClientFactory
{
	/**
	 * Create or retrieve a client for the service interface provided<br />
	 * This method only works where a single service for this interface has been configured. For more complex situations, use
	 * <code>getClient(iface, name)</code>
	 *
	 * @param iface
	 *
	 * @return
	 */
	public <T> T getClient(final Class<T> iface);

	/**
	 * Create or retrieve a client for the service interface, with the underlying REST service identified by <code>name</code>
	 *
	 * @param iface
	 * @param name
	 * 		the service name
	 *
	 * @return
	 */
	public <T> T getClient(final Class<T> iface, String name);
}
