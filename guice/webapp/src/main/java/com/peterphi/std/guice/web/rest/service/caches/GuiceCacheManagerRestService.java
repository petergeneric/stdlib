package com.peterphi.std.guice.web.rest.service.caches;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/guice/caches")
@ServiceName("Caches")
@Doc("Displays cache information and allows them to be invalidated")
@ImplementedBy(GuiceCacheManagerRestServiceImpl.class)
public interface GuiceCacheManagerRestService
{
	@GET
	@Path("/")
	@Produces("text/html")
	String getIndex(@QueryParam("message") @Doc("for internal use") String message);

	@POST
	@Path("/invalidate")
	@Produces("text/html")
	@Doc("Invalidate cached values")
	Response invalidate(@FormParam("csrf_token") String providedCsrfToken,
	                    @FormParam("name") @Doc("If omitted will invalidate all caches") String cacheName);
}
