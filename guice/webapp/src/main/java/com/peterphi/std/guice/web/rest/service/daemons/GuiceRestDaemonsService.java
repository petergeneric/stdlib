package com.peterphi.std.guice.web.rest.service.daemons;

import com.google.inject.ImplementedBy;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.annotation.ServiceName;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/guice/threads")
@ServiceName("Threads")
@Doc("Indexes all guice daemons and centrally-launched threads")
@ImplementedBy(GuiceRestDaemonsServiceImpl.class)
public interface GuiceRestDaemonsService
{
	@GET
	@Path("/")
	@Produces("text/html")
	public String getIndex();
}
