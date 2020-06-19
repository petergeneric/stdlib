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
import java.util.List;

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
	Response create(@FormParam("token") final String token,
	                @FormParam("name") final String name,
	                @FormParam("required_role") final String requiredRoleName,
	                @FormParam("endpoints") final String endpoints,
	                @FormParam("roles") final List<String> roles);

	@POST
	@Path("/service/{id}/disable")
	@Produces(MediaType.TEXT_HTML)
	Response disable(@PathParam("id") String id, @FormParam("token") final String token);

	@POST
	@Path("/service/{id}/edit")
	@Produces(MediaType.TEXT_HTML)
	Response edit(@FormParam("token") final String token,
	              @PathParam("id") String id,
	              @FormParam("required_role") final String requiredRoleName,
	              @FormParam("endpoints") final String endpoints,
	              @FormParam("roles") final List<String> roles);


	@POST
	@Path("/service/{service_id}/rotate-access-key")
	@Produces(MediaType.TEXT_HTML)
	Response rotateAccessKey(@PathParam("service_id") String serviceId, @FormParam("token") String token);
}
