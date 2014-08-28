package com.peterphi.std.guice.metrics.rest.api;

import com.google.inject.ImplementedBy;
import com.peterphi.std.guice.metrics.rest.impl.HealthRestServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guice/health")
@ImplementedBy(HealthRestServiceImpl.class)
public interface HealthRestService
{
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String get();

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public String getHTML();
}
