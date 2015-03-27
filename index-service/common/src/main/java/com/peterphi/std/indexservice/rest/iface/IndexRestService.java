package com.peterphi.std.indexservice.rest.iface;

import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;
import com.peterphi.std.indexservice.rest.type.*;

import javax.ws.rs.*;

@Path("/index-service")
@FastFailServiceClient
public interface IndexRestService
{
	/**
	 * Register a new application
	 *
	 * @param request
	 *
	 * @return
	 */
	@POST
	@Path("/applications")
	@Produces("application/xml")
	@Consumes("application/xml")
	public RegistrationResponse registerApplication(RegistrationRequest request);

	/**
	 * Retrieve a list of application ids
	 *
	 * @return
	 */
	@GET
	@Path("/applications")
	@Produces("application/xml")
	public ApplicationSearchResults getAllApplications();

	/**
	 * Unregister an application
	 *
	 * @param applicationId
	 */
	@POST
	@Path("/application/unregister/{application_id}")
	@Produces("application/xml")
	public UnregisterResponse deleteApplication(@PathParam("application_id") String applicationId);

	/**
	 * Retrieve all services for an application
	 *
	 * @param applicationId
	 *
	 * @return
	 */
	@GET
	@Path("/application/{application_id}")
	@Produces("application/xml")
	public ServiceSearchResults getServicesForApplication(@PathParam("application_id") String applicationId);


	/**
	 * Re-register an application
	 *
	 * @param request
	 *
	 * @return
	 */
	@PUT
	@Path("/application/{application_id}")
	@Produces("application/xml")
	@Consumes("application/xml")
	public RegistrationResponse reregisterApplication(@PathParam("application_id") String applicationId,
	                                                  RegistrationRequest request);


	/**
	 * Heartbeat an application (to keep its services active)
	 *
	 * @param applicationId
	 *
	 * @return
	 */
	@POST
	@Path("/application/{application_id}/heartbeat")
	@Produces("application/xml")
	public RegistrationHeartbeatResponse heartbeatApplication(@PathParam("application_id") String applicationId);

	/**
	 * Find all active services meeting some constraints
	 *
	 * @param request
	 *
	 * @return
	 */
	@POST
	@Path("/services/search")
	@Produces("application/xml")
	@Consumes("application/xml")
	public ServiceSearchResults searchForServices(ServiceSearchRequest request);

	/**
	 * Return all active services
	 *
	 * @return
	 */
	@GET
	@Path("/services")
	@Produces("application/xml")
	public ServiceSearchResults getAllServices();
}