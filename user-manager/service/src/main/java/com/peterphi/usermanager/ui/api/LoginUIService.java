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
	Response doLogin(@FormParam("token") String token,
	                 @FormParam("returnTo") @DefaultValue("/") String returnTo,
	                 @FormParam("email") String user,
	                 @FormParam("password") String password);

	@GET
	@NoCache
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	Response doLogout(@QueryParam("returnTo") @DefaultValue("") String returnTo);


	@GET
	@Path("/reset")
	@Produces(MediaType.TEXT_HTML)
	String doPasswordReset(@QueryParam("code") String code);

	@POST
	@Path("/reset")
	@Produces(MediaType.TEXT_HTML)
	Response doPasswordReset(@FormParam("code") String code,
	                         @FormParam("token") @Doc("CSRF Token") String csrfToken,
	                         @FormParam("new_password") String newPassword,
	                         @FormParam("new_password_confirm") String newPasswordConfirm);
}
