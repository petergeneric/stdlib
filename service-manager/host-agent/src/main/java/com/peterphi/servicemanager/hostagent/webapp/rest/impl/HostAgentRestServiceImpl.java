package com.peterphi.servicemanager.hostagent.webapp.rest.impl;

import com.peterphi.servicemanager.hostagent.rest.iface.HostAgentRestService;
import com.peterphi.servicemanager.hostagent.rest.type.DeployUpdatedCerts;
import com.peterphi.servicemanager.hostagent.rest.type.DeployWebappRequest;
import com.peterphi.std.NotImplementedException;

public class HostAgentRestServiceImpl implements HostAgentRestService
{
	@Override
	public void updateCertificates(final DeployUpdatedCerts request)
	{
		// TODO write the updated certificate files (N.B. permissions won't let us do this so we'll have to change them)
		throw new NotImplementedException("Functionality not developed yet");
	}


	@Override
	public void deployWebapp(final DeployWebappRequest request)
	{
		throw new NotImplementedException("Functionality not developed yet");
	}
}
