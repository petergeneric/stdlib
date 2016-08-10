package com.peterphi.std.guice.web.rest.auth.oauth2.rest.api;

import com.peterphi.std.annotation.Doc;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/oauth2")
public interface OAuth2ClientCallbackRestService
{
	@GET
	@Path("/cb")
	@Doc("OAuth2 callback with authorisation code")
	Response callback(@QueryParam("code") String code, @QueryParam("returnTo") String returnTo);
}

