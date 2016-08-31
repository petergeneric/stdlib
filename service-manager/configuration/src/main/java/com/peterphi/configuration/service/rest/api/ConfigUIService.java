package com.peterphi.configuration.service.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
	String getIndex();

	@GET
	@Path("/edit")
	@Produces(MediaType.TEXT_HTML)
	String getRootConfigPage();

	@GET
	@Path("/edit/{path:.*}")
	@Produces(MediaType.TEXT_HTML)
	String getConfigPage(@PathParam("path") String path);

	@POST
	@Path("/create-path")
	@Produces(MediaType.TEXT_HTML)
	Response getConfigPage(@FormParam("_nonce") String nonce,
	                       @FormParam("parent_path") String path,
	                       @FormParam("child_path") String child);

	@POST
	@Path("/import-properties")
	@Produces(MediaType.TEXT_HTML)
	Response importPropertyFile(@FormParam("_nonce") String nonce,
	                            @FormParam("_path") String path,
	                            @FormParam("properties") String properties,
	                            @FormParam("_message") String message);

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	Response applyChanges(MultivaluedMap<String, String> fields);


	@POST
	@Path("/pull-remote")
	@Produces(MediaType.TEXT_HTML)
	Response pullRemote();
}
