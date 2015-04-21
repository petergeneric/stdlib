package com.peterphi.std.guice.metrics.rest.api;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.metrics.rest.impl.HealthRestServiceImpl;
import com.peterphi.std.guice.metrics.rest.types.HealthDocument;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guice/health")
@ImplementedBy(HealthRestServiceImpl.class)
@FastFailServiceClient
@ServiceName("Health")
@Doc("UI and API for health metrics for this service")
public interface HealthRestService
{
	@GET
	@Path("/")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public HealthDocument get();

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public String getHTML();
}
