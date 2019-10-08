package com.peterphi.std.guice.web.rest.service.breaker;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
}
