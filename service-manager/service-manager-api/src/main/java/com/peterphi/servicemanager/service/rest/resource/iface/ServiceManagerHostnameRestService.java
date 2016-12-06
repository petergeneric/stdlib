package com.peterphi.servicemanager.service.rest.resource.iface;

import com.peterphi.servicemanager.service.rest.resource.type.HostnameRequestDTO;
import com.peterphi.servicemanager.service.rest.resource.type.HostnameResponseDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hostnames")
public interface ServiceManagerHostnameRestService
{
	@POST
	@Path("/allocate")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	HostnameResponseDTO allocateHostname(HostnameRequestDTO request);

	@POST
	@Path("/refresh")
	@Produces(MediaType.APPLICATION_XML)
	HostnameResponseDTO refreshHostname(@FormParam("management-token") final String managementToken,
	                                    @FormParam("hostname") final String hostname);
}
