package com.peterphi.std.guice.metrics.rest.api;

import com.google.inject.ImplementedBy;
import com.peterphi.std.guice.metrics.rest.impl.HealthRestServiceImpl;
import com.peterphi.std.guice.metrics.rest.impl.MetricsRestServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/guice/health")
@ImplementedBy(HealthRestServiceImpl.class)
public interface HealthRestService
{
	@Produces("text/plain")
	@Path("/")
	@GET
	public String get();
}
