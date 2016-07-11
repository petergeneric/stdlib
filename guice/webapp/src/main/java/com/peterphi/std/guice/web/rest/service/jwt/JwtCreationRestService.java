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

@Path("/guice/jwt")
@ServiceName("JWT")
@Doc("JWT auth token helper; allows generating and/or saving JWTs (using a user-supplied secret value)")
@ImplementedBy(JwtCreationRestServiceImpl.class)
public interface JwtCreationRestService
{
	@GET
	@Path("/")
	@Produces("text/html")
	String getIndex(@QueryParam("message") @Doc("for internal use") String message);

	@POST
	@Path("/")
	@Produces("text/html")
	String getResult(@FormParam("token") String token,
	                 @FormParam("secret") String secret,
	                 @FormParam("payload") String payload,
	                 @Doc("operation - Generate or Save") @FormParam("op") String op);
}
