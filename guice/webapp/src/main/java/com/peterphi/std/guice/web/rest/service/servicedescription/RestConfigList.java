package com.peterphi.std.guice.web.rest.service.servicedescription;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.restclient.annotations.FastFailServiceClient;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Doc("Lists the configuration properties in use by this webapp")
@Path("/guice/config")
@ImplementedBy(RestConfigListImpl.class)
@FastFailServiceClient
public interface RestConfigList
{
	@Doc("Lists all config")
	@GET
	@Produces("text/html")
	@Path("/")
	public String index() throws Exception;

	@Doc("Reconfigures an existing config value")
	@POST
	@Produces("text/html")
	@Path("/reconfigure")
	public String setProperty(@FormParam("key") String name, @FormParam("value") String value);

	@Doc("Validates a potential config value change")
	@POST
	@Produces("application/json")
	@Path("/validate")
	public boolean validateProperty(@FormParam("key") String name, @FormParam("value") String value);

	@Doc("Saves the override configuration to disk")
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Path("/save")
	public String save() throws IOException;
}
