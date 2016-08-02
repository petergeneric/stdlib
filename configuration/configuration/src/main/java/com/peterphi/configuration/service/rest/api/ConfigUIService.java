package com.peterphi.configuration.service.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path("/")
public interface ConfigUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	Response getIndex();

	@GET
	@Path("/config/edit")
	@Produces(MediaType.TEXT_HTML)
	String getRootConfigPage();

	@GET
	@Path("/config/edit/{path:.*}")
	@Produces(MediaType.TEXT_HTML)
	String getConfigPage(@PathParam("path") String path);

	@POST
	@Path("/config/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	Response applyChanges(MultivaluedMap<String, String> fields);
}
