package com.peterphi.std.guice.testrestclient.server;

import com.peterphi.std.annotation.Doc;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/test")
public interface MyTestService
{
	@GET
	@Path("/")
	@Produces("text/html")
	public String indexPage();

	@GET
	@Doc("Recognises whether the supplied date is that of  the WW2 pearl harbour attack")
	@Path("/date")
	@Produces("text/html")
	public String datePage(@QueryParam("date") DateTime date);

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
