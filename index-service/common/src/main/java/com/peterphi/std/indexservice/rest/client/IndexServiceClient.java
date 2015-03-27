package com.peterphi.std.indexservice.rest.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import com.peterphi.std.indexservice.rest.iface.IndexRestService;
import com.peterphi.std.indexservice.rest.type.*;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A convenient abstraction over an index REST service.
 */
@Singleton
@FastFailServiceClient
public class IndexServiceClient
{
	private static final Logger log = Logger.getLogger(IndexServiceClient.class);

	/**
	 * Time to sleep & then retry when an API call fails
	 */
	private Timeout apiCallRetryTime = new Timeout(10, TimeUnit.SECONDS);

	/**
	 * The number of times we are willing to retry API calls that fail
	 */
	private int apiCallRetryLimit = 3;

	@Inject
	private IndexRestService service;

	@Inject
	private JAXRSProxyClientFactory clientFactory;

	/**
	 * Convenience wrapper for gather-scatter calls
	 * @param iface
	 * @param properties
	 * @param <T>
	 * @return
	 */
	public <T> List<T> findServices(Class<T> iface,PropertyList properties) {
		List<T> results = new ArrayList<>();
		List<ServiceDescription> searchMatches = findServiceDescriptions(iface,properties);
		for(ServiceDescription serviceDescription : searchMatches) {
			results.add(clientFactory.createClient(iface,serviceDescription.details.endpoint));
		}
		return results;
	}
	/**
	 *
	 * @param iface
	 * @param <T>
	 * @return
	 * @throws NoServiceFoundException
	 */
	public <T> T findService(Class<T> iface) throws NoServiceFoundException
	{
		URI endpoint = findServiceEndpoint(iface);
		return clientFactory.createClient(iface,endpoint);
	}
	/**
	 * Look up a service
	 *
	 * @param iface
	 *
	 * @return
	 *
	 * @throws NoServiceFoundException
	 */
	public URI findServiceEndpoint(Class<?> iface) throws NoServiceFoundException
	{
		List<ServiceDescription> results = findServiceDescriptions(iface);

		// Pick the "best" service (currently, picks a random service)
		// Randomly order the results
		Collections.shuffle(results);

		for (ServiceDescription service : results)
		{
			log.debug("Resolved " +
			          iface.getSimpleName() +
			          " to " +
			          service.details.endpoint +
			          " provided by " +
			          service.applicationId +
			          " (" +
			          results.size() +
			          " candidate[s])");

			return URI.create(service.details.endpoint);
		}

		throw new NoServiceFoundException("Index service knows no instance of " + iface.getName());
	}

    public List<ServiceDescription> findServiceDescriptions(Class<?> iface)
    {
        return findServiceDescriptions(iface, new PropertyList());
    }

	/**
	 * Try to find services, automatically retrying in the case of a communication failure
	 *
	 * @param iface
	 *
	 * @return
	 */
	public List<ServiceDescription> findServiceDescriptions(Class<?> iface,PropertyList properties)
	{
		final ServiceSearchRequest request = new ServiceSearchRequest();
		request.iface = iface.getName();
        request.properties = properties;

		for (int i = 0; i < apiCallRetryLimit; i++)
		{
			if (i != 0)
				apiCallRetryTime.sleep(); // wait before we try again

			try
			{
				final ServiceSearchResults results = service.searchForServices(request);

				return results.services;
			}
			catch (Exception e)
			{
				log.warn("Index service search failed", e);
			}
		}

		// Try one last time without failure catching logic
		final ServiceSearchResults results = service.searchForServices(request);
		return results.services;
	}

	public RegistrationHeartbeatResponse heartbeat(final String applicationId)
	{
		for (int i = 0; i < apiCallRetryLimit; i++)
		{
			if (i != 0)
				apiCallRetryTime.sleep(); // wait before we try again

			try
			{
				return service.heartbeatApplication(applicationId);
			}
			catch (Exception e)
			{
				log.warn("Index service heartbeat failed", e);
			}
		}

		// Try one last time without failure catching logic
		return service.heartbeatApplication(applicationId);
	}

	public RegistrationResponse reregister(final String applicationId, final RegistrationRequest request)
	{
		for (int i = 0; i < apiCallRetryLimit; i++)
		{
			if (i != 0)
				apiCallRetryTime.sleep(); // wait before we try again

			try
			{
				return service.reregisterApplication(applicationId, request);
			}
			catch (Exception e)
			{
				log.warn("Index service reregister failed", e);
			}
		}

		// Try one last time without failure catching logic
		return service.reregisterApplication(applicationId, request);
	}

	public RegistrationResponse register(final RegistrationRequest request)
	{
		for (int i = 0; i < apiCallRetryLimit; i++)
		{
			if (i != 0)
				apiCallRetryTime.sleep(); // wait before we try again

			try
			{
				return service.registerApplication(request);
			}
			catch (Exception e)
			{
				log.warn("Index service register failed", e);
			}
		}

		// Try one last time without failure catching logic
		return service.registerApplication(request);
	}

	/**
	 * Try to unregister without any retry on failure
	 *
	 * @param applicationId
	 */
	public UnregisterResponse unregister(final String applicationId)
	{
		return service.deleteApplication(applicationId);
	}
}