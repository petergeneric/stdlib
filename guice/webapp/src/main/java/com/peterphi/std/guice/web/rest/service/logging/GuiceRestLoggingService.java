package com.peterphi.std.guice.web.rest.service.logging;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/guice/logging")
@Doc("Allows the log4j log levels to be modified at runtime")
@ServiceName("Logging")
@ImplementedBy(GuiceRestLoggingServiceImpl.class)
public interface GuiceRestLoggingService
{
	@GET
	@Path("/")
	@Produces("text/html")
	String getIndex();


	@POST
	@Path("/load-config-file")
	@Produces("text/html")
	@Doc("Called to load a log4j.properties resource")
	Response loadConfigFile(@FormParam("resource") String resource);

	@POST
	@Path("/load-config-string")
	@Produces("text/html")
	@Doc("Called to load a log4j.properties configuration directly")
	Response loadConfig(@FormParam("log4j.properties") String configuration) throws IOException;
}
