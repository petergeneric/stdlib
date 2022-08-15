package com.peterphi.usermanager.ui.api;

import org.jboss.resteasy.annotations.cache.NoCache;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public interface LoginUIService
{
	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	String getLogin(@QueryParam("returnTo") @DefaultValue("/") String returnTo, @QueryParam("errorText") String errorText);

	@POST
	@Path("/login")
	@Produces(MediaType.TEXT_HTML)
	Response doLogin(@FormParam("token") String token,
	                 @FormParam("returnTo") @DefaultValue("/") String returnTo,
	                 @FormParam("email") String user,
	                 @FormParam("password") String password);

	@GET
	@NoCache
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	Response doLogout(@QueryParam("returnTo") @DefaultValue("") String returnTo);
}
