package com.peterphi.std.indexservice.rest.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.indexservice.rest.iface.IndexRestService;
import com.peterphi.std.indexservice.rest.type.RegistrationHeartbeatResponse;
import com.peterphi.std.indexservice.rest.type.RegistrationRequest;
import com.peterphi.std.indexservice.rest.type.RegistrationResponse;
import com.peterphi.std.indexservice.rest.type.ServiceDescription;
import com.peterphi.std.indexservice.rest.type.ServiceSearchRequest;
import com.peterphi.std.indexservice.rest.type.ServiceSearchResults;
import com.peterphi.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A convenient abstraction over an index REST service.
 */
@Singleton
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


	protected final IndexRestService service;

	@Inject
	public IndexServiceClient(IndexRestService service)
	{
		this.service = service;
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
		List<ServiceDescription> results = findServices(iface);

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

	/**
	 * Try to find services, automatically retrying in the case of a communication failure
	 *
	 * @param iface
	 *
	 * @return
	 */
	public List<ServiceDescription> findServices(Class<?> iface)
	{
		final ServiceSearchRequest request = new ServiceSearchRequest();
		request.iface = iface.getName();

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
	public void unregister(final String applicationId)
	{
		service.deleteApplication(applicationId);
	}
}
