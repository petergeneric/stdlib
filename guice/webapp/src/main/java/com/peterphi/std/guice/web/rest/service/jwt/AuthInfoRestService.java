package com.peterphi.std.guice.web.rest.service.jwt;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

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
