package com.peterphi.servicemanager.service.rest.ui.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by bmcleod on 13/09/2016.
 */
@Path("/rules")
public interface RulesUIService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	String getIndex();
}
