package com.peterphi.std.guice.web.rest.service.logging;

import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/guice/logging")
@Doc("Allows the log4j log levels to be modified at runtime")
@ServiceName("Logging")
public interface GuiceRestLoggingService
{
	@GET
	@Path("/")
	@Produces("text/html")
	public String getIndex();
}
