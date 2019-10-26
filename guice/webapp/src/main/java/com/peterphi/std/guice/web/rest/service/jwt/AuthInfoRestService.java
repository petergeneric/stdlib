package com.peterphi.std.guice.web.rest.service.jwt;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/guice/auth")
@ServiceName("Auth")
@Doc("Authentication / Authorisation Status")
@ImplementedBy(AuthInfoRestServiceImpl.class)
public interface AuthInfoRestService
{
	@GET
	@Path("/")
	@Produces("text/html")
	String getIndex(@QueryParam("message") @Doc("for internal use") String message);

	@GET
	@Path("/test")
	@Produces("text/plain")
	@Doc("Test page controlled by an auth constraint; returns OK if the user is authenticated")
	String getTestPage();

	@POST
	@Path("/")
	@Produces("text/html")
	String saveJWTCookie(@FormParam("token") String token, @Doc("operation - must be Save") @FormParam("op") String op);
}
