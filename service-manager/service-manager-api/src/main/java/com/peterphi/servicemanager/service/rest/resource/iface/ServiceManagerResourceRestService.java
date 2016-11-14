package com.peterphi.servicemanager.service.rest.resource.iface;

import com.peterphi.servicemanager.service.rest.resource.type.ProvisionResourceParametersDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceDTO;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceTemplateDTO;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/resources")
public interface ServiceManagerResourceRestService
{
	@GET
	@Path("/instance/{id}")
	@Produces(MediaType.APPLICATION_XML)
	ResourceInstanceDTO getInstanceById(@PathParam("id") final int id);

	@DELETE
	@Path("/instance/{id}")
	void discardInstance(@PathParam("id") final int id);

	@POST
	@Path("/template/{template_name}/provision")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	ResourceInstanceDTO provision(@PathParam("template_name") final String templateName,
	                              final ProvisionResourceParametersDTO parameters);

	@GET
	@Path("/template/{template_name}")
	@Produces(MediaType.APPLICATION_XML)
	ResourceTemplateDTO getTemplateById(@PathParam("template_name") final String templateName);

	@POST
	@Path("/instances")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	List<ResourceInstanceDTO> searchInstances(WebQuery query);

	@GET
	@Path("/instances")
	@Produces(MediaType.APPLICATION_XML)
	List<ResourceInstanceDTO> searchInstances(@Context UriInfo info);

	@GET
	@Path("/templates")
	@Produces(MediaType.APPLICATION_XML)
	List<ResourceTemplateDTO> getTemplates();
}
