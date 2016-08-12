package com.peterphi.usermanager.ui.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public interface ServiceUIService
{
	@GET
	@Path("/services")
	@Produces(MediaType.TEXT_HTML)
	String getList(@Context UriInfo query);

	@GET
	@Path("/service/{id}")
	@Produces(MediaType.TEXT_HTML)
	String get(@PathParam("id") String id);

	@POST
	@Path("/services/create")
	@Produces(MediaType.TEXT_HTML)
	Response create(@FormParam("nonce") final String nonce,
	                @FormParam("name") final String name,
	                @FormParam("endpoints") final String endpoints);

	@POST
	@Path("/service/{id}/disable")
	@Produces(MediaType.TEXT_HTML)
	Response disable(@PathParam("id") String id, @FormParam("nonce") final String nonce);

	@POST
	@Path("/service/{id}/edit-endpoints")
	@Produces(MediaType.TEXT_HTML)
	Response setEndpoints(@FormParam("nonce") final String nonce,
	                      @PathParam("id") String id,
	                      @FormParam("endpoints") final String endpoints);
}
