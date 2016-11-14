package com.peterphi.servicemanager.service.rest.ui.api;

import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface ServiceManagerUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	Response getIndex();


	@GET
	@Path("/logs/search")
	String getSearchLogs(@QueryParam("from") String from,
	                     @QueryParam("to") String to,
	                     @QueryParam("minLevel") @DefaultValue("2") final int minLevel);

	@GET
	@Path("/logs/tail")
	String getTail();

	@POST
	@Path("/logs/tail.do")
	public Response doTail(@FormParam("id") final String subscriptionId) throws Exception;

	@GZIP
	@POST
	@Path("/logs/search.do")
	public Response doSearchLogs(@FormParam("from") String fromStr,
	                             @FormParam("to") String toStr,
	                             @FormParam("filter") String filter);
}
