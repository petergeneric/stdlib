package com.peterphi.std.guice.metrics.rest.api;

import com.google.inject.ImplementedBy;
import com.peterphi.std.guice.metrics.rest.impl.MetricsRestServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guice/metrics")
@ImplementedBy(MetricsRestServiceImpl.class)
public interface MetricsRestService
{
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	@GET
	public String get();
}
