package com.peterphi.usermanager.rest.iface;

import com.peterphi.usermanager.rest.type.UserManagerUser;
import com.peterphi.std.annotation.Doc;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Doc("User service")
public interface UserManagerRestService
{
	@GET
	@Path("/user/{user_id}")
	@Produces(MediaType.APPLICATION_XML)
	UserManagerUser get(@PathParam("user_id") int id);

	@POST
	@Path("/users/check-credentials")
	@Produces(MediaType.APPLICATION_XML)
	UserManagerUser login(@FormParam("email") String email, @FormParam("password") String password);
}
