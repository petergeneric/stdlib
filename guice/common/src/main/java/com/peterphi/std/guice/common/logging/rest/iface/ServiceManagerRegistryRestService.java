package com.peterphi.std.guice.common.logging.rest.iface;

import com.peterphi.std.annotation.Doc;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/registry")
public interface ServiceManagerRegistryRestService
{
	@POST
	@Path("/register")
	@Doc("Register an instance of a service")
	void register(@FormParam("instance-id") final String instanceId,
	              @FormParam("endpoint") final String endpoint,
	              @FormParam("mgmt-bearer-token") final String managementToken,
	              @FormParam("code-rev") final String codeRevision);
}
