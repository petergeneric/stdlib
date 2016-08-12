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
public interface RoleUIService
{
	@GET
	@Path("/roles")
	@Produces(MediaType.TEXT_HTML)
	String getRoles(@Context UriInfo query);

	@GET
	@Path("/role/{role_id}")
	@Produces(MediaType.TEXT_HTML)
	String get(@PathParam("role_id") String roleId);

	@POST
	@Path("/roles/create")
	@Produces(MediaType.TEXT_HTML)
	Response create(@FormParam("id") final String id,
	                @FormParam("nonce") String nonce,
	                @FormParam("caption") final String caption);

	@POST
	@Path("/role/{role_id}/delete")
	@Produces(MediaType.TEXT_HTML)
	Response delete(@PathParam("role_id") String roleId, @FormParam("nonce") String nonce);

	@POST
	@Path("/role/{role_id}/change-caption")
	@Produces(MediaType.TEXT_HTML)
	Response changeCaption(@PathParam("role_id") String roleId,
	                       @FormParam("nonce") String nonce,
	                       @FormParam("caption") final String newPassword);

	@POST
	@Path("/role/{role_id}/change-members")
	@Produces(MediaType.TEXT_HTML)
	Response changeMembers(@PathParam("role_id") String roleId, @FormParam("nonce") String nonce, @FormParam("members")

	final List<Integer> members);
}
