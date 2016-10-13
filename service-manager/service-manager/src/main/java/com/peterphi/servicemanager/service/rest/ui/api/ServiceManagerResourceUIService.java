package com.peterphi.servicemanager.service.rest.ui.api;

import com.peterphi.std.annotation.Doc;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/resources")
public interface ServiceManagerResourceUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	String getResources();


	@GET
	@Path("/template/{id}")
	@Produces(MediaType.TEXT_HTML)
	String getResource(@PathParam("id") String id);


	@Doc("Provision a new instance of the named template")
	@POST
	@Path("/templates/new-instance")
	@Produces(MediaType.TEXT_HTML)
	Response doProvision(@FormParam("id") String id, @FormParam("nonce") String nonce);

	@POST
	@Path("/resource-instances/discard")
	@Produces(MediaType.TEXT_HTML)
	Response doDiscard(@FormParam("id") int id, @FormParam("nonce") String nonce);
}
