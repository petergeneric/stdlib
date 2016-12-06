package com.peterphi.servicemanager.hostagent.rest.iface;

import com.peterphi.servicemanager.hostagent.rest.type.DeployUpdatedCerts;
import com.peterphi.servicemanager.hostagent.rest.type.DeployWebappRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface HostAgentRestService
{
	@POST
	@Path("/deploy-certificates.do")
	@Consumes(MediaType.APPLICATION_XML)
	void updateCertificates(DeployUpdatedCerts request);

	@POST
	@Path("/deploy-webapp.do")
	@Consumes(MediaType.APPLICATION_XML)
	void deployWebapp(DeployWebappRequest request);

	@POST
	@Path("/reindex-webapps.do")
	void reindexWebapps();
}
