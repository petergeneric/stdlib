package com.peterphi.std.guice.common.logging.rest.iface;

import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.logging.logreport.LogReport;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/logging")
public interface ServiceManagerLoggingRestService
{
	@POST
	@Path("/report")
	@Consumes("application/octet-stream+log-report")
	@Doc("Report logs for a service which has already been registered with the Service Manager")
	void report(final LogReport logs);
}
