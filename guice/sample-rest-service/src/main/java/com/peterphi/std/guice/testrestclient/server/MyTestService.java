package com.peterphi.std.guice.testrestclient.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/test")
public interface MyTestService
{
	@GET
	@Path("/")
	@Produces("text/html")
	public String indexPage();

	@GET
	@Path("/")
	@Produces("application/xml")
	public String index();

	@GET
	@Path("/fail")
	@Produces("text/plain")
	public String fail();


	@GET
	@Path("/fail2")
	@Produces("text/plain")
	public String fail2();
}
