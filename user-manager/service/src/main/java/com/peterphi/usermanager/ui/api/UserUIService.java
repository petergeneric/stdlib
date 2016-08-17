package com.peterphi.usermanager.ui.api;

import com.peterphi.std.annotation.Doc;

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

@Doc("User Manager user interface")
@Path("/")
public interface UserUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	String getIndex();

	@GET
	@Path("/users")
	@Produces(MediaType.TEXT_HTML)
	String getUsers(@Context UriInfo query);

	@GET
	@Path("/user/{user_id}")
	@Produces(MediaType.TEXT_HTML)
	String getUserEdit(@PathParam("user_id") int userId);

	@POST
	@Path("/user/{user_id}")
	@Produces(MediaType.TEXT_HTML)
	Response editUserProfile(@PathParam("user_id") int userId,
	                         @FormParam("nonce") String nonce,
	                         @FormParam("dateFormat") String dateFormat,
	                         @FormParam("timeZone") String timeZone,
	                         @FormParam("name") String name,
	                         @FormParam("email") String email,
	                         @FormParam("roles") List<String> roles);

	@POST
	@Path("/user/{user_id}/delete")
	@Produces(MediaType.TEXT_HTML)
	Response deleteUser(@PathParam("user_id") int userId, @FormParam("nonce") String nonce);

	@POST
	@Path("/user/{user_id}/change_password")
	@Produces(MediaType.TEXT_HTML)
	Response changePassword(@PathParam("user_id") int userId,
	                        @FormParam("nonce") String nonce,
	                        @FormParam("password") final String newPassword,
	                        @FormParam("passwordConfirm") final String newPasswordConfirm);

	@POST
	@Path("/user/{user_id}/impersonate")
	@Produces(MediaType.TEXT_HTML)
	Response impersonate(@PathParam("user_id") int userId, @FormParam("nonce") String nonce);
}
