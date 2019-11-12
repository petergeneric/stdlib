package com.peterphi.usermanager.ui.api;

import com.peterphi.std.annotation.Doc;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	Response doLogin(@Doc("CSRF protection code") @FormParam("nonce") String nonce,
	                 @FormParam("returnTo") @DefaultValue("/") String returnTo,
	                 @FormParam("email") String user,
	                 @FormParam("password") String password);

	@GET
	@NoCache
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	@Doc("Start the logout flow (or confirm that the user is logged out if already logged out)")
	String requestLogout(@QueryParam("returnTo") @DefaultValue("") String returnTo);

	@POST
	@NoCache
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	@Doc("Perform the logout action")
	Response doLogout(@Doc("CSRF protection id") @FormParam("nonce") String nonce,
	                  @FormParam("returnTo") @DefaultValue("") String returnTo);
}
