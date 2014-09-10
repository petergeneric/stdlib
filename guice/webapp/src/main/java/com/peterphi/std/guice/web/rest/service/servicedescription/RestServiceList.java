package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Doc("Lists the REST services and resources exposed by this webapp")
@Path("/guice")
@ImplementedBy(RestServiceListImpl.class)
@FastFailServiceClient
public interface RestServiceList
{
	@Doc("Lists all services")
	@GET
	@Produces("text/html")
	@Path("/")
	public String index(@Context HttpHeaders headers, @Context UriInfo uriInfo) throws Exception;

	@Doc(value = "Describes a single service")
	@GET
	@Produces("text/html")
	@Path("/service/{service_id}")
	public String getServiceDescription(@Doc(value = "the internal index of the service") @PathParam("service_id") int serviceId,
	                                    @Context HttpHeaders headers,
	                                    @Context UriInfo uriInfo) throws Exception;
}
