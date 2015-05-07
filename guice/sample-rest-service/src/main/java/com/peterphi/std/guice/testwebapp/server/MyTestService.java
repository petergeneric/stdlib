package com.peterphi.std.guice.testwebapp.server;

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
	@Path("/manifest")
	@Produces("text/plain")
	@Doc("Attempt to parse and return the MANIFEST.MF file")
	public String manifest() throws Exception;

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

	@GET
	@Path("/auth-test/reject")
	@Produces("text/plain")
	public String authReject();

	@GET
	@Path("/auth-test/skip")
	@Produces("text/plain")
	public String authSkip();
}
