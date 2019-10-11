package com.peterphi.std.guice.web.rest.service.breaker;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/guice/breakers")
@ServiceName("Breakers")
@Doc("Displays breakers and allows them to be tripped or reset")
@ImplementedBy(GuiceRestBreakerServiceImpl.class)
public interface GuiceRestBreakerService
{
	@GET
	@Path("/")
	@Produces("text/html")
	String getIndex(@QueryParam("message") @Doc("for internal use") String message);

	@POST
	@Path("/set-breaker-state")
	@Produces("text/html")
	@Doc("Trip or reset a breaker")
	Response trigger(@FormParam("name") final String name,
	                 @FormParam("value") @DefaultValue("false") final boolean value,
	                 @FormParam("note") String note);

	@GET
	@Path("/breaker/{breaker_name}/test")
	@Produces("text/plain")
	@Doc("Returns 2xx with body 'OK' if breaker is not tripped, otherwise 503 with body 'Tripped'. If no such breaker then returns code 404. No auth is required for this method.")
	Response testBreaker(@PathParam("breaker_name") String breakerName);
}
