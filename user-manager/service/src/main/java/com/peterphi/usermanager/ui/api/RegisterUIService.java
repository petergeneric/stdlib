package com.peterphi.usermanager.ui.api;

import com.peterphi.std.annotation.Doc;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/register")
@Doc("Handles new user registration")
public interface RegisterUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	String getRegister();

	@POST
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	Response doRegister(@FormParam("nonce") String nonce,
	                    @FormParam("email") String email,
	                    @FormParam("name") String name,
	                    @FormParam("dateFormat") String dateFormat,
	                    @FormParam("timeZone") String timeZone,
	                    @FormParam("password") String password,
	                    @FormParam("passwordConfirm") String passwordConfirm,
	                    @FormParam("roles") List<String> roles);
}
