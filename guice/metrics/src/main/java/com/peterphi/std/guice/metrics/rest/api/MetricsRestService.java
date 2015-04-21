package com.peterphi.std.guice.metrics.rest.api;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;
import com.peterphi.std.guice.metrics.rest.impl.MetricsRestServiceImpl;
import com.peterphi.std.guice.metrics.rest.types.MetricsDocument;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guice/metrics")
@ImplementedBy(MetricsRestServiceImpl.class)
@FastFailServiceClient
@ServiceName("Metrics")
@Doc("UI and API for service metrics")
public interface MetricsRestService
{
	@GET
	@Path("/")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public MetricsDocument getMetrics();


	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTextMetrics();

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public String getIndex();
}
