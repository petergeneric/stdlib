package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Doc("Lists the configuration properties in use by this webapp")
@Path("/list/config")
@ImplementedBy(RestConfigListImpl.class)
public interface RestConfigList
{
	@Doc("Lists all config")
	@GET
	@Produces("text/html")
	@Path("/")
	public String index(@Context HttpHeaders headers, @Context UriInfo uriInfo) throws Exception;
}
