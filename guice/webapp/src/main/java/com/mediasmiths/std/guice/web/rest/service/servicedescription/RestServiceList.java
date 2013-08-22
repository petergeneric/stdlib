package com.mediasmiths.std.guice.web.rest.service.servicedescription;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.annotation.Doc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Doc("Lists the REST services and resources exposed by this webapp")
@Path("/list")
@ImplementedBy(RestServiceListImpl.class)
public interface RestServiceList
{
	@Doc("Lists all services")
	@GET
	@Produces("text/html")
	@Path("/")
	public String index() throws Exception;

	@Doc("Describes a single service")
	@GET
	@Produces("text/html")
	@Path("/service/{service_id}")
	public String getServiceDescription(@Doc("the internal index of the service") @PathParam("service_id") int serviceId) throws Exception;
}
