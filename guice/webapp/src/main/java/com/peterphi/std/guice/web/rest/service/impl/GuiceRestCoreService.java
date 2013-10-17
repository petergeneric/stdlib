package com.peterphi.std.guice.web.rest.service.impl;

import com.peterphi.std.annotation.Doc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Doc("Core framework development services")
@Path("/guice/rest-core")
public interface GuiceRestCoreService
{
	@Doc("Returns Http 200 when called")
	@GET
	@Path("/ping")
	public abstract String ping();

	@Doc("Returns the service configuration currently in memory (or fails if this functionality has been disabled)")
	@GET
	@Path("/service.properties")
	@Produces("text/plain")
	public abstract String properties() throws Exception;

	@Doc("Restarts the Guice environment within this webapp without restarting the webapp (or fails if this functionality has been disabled)")
	@GET
	@Path("/restart")
	public abstract String restart() throws Exception;

}
