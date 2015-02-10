package com.peterphi.std.guice.web.rest.service.restcore;

import com.google.inject.ImplementedBy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/guice")
@ImplementedBy(GuiceCommonRestResourcesImpl.class)
public interface GuiceCommonRestResources
{
	@GET
	@Path("/bootstrap.css")
	@Produces("text/css")
	public String getBootstrapCSS();
}
