package com.mediasmiths.std.indexservice.rest.impl;

import com.mediasmiths.std.indexservice.rest.type.RegistrationRequest;
import com.mediasmiths.std.indexservice.rest.type.ServiceDescription;
import com.mediasmiths.std.indexservice.rest.type.ServiceDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisteredApp
{
	public String applicationId;
	public String applicationName;

	public List<ServiceDescription> services = new ArrayList<ServiceDescription>();

	/**
	 * The date after which this application should no longer be considered active
	 */
	public Date nextHeartbeatDue;

	/**
	 * The date of the last heartbeat
	 */
	public Date lastHeartbeat;

	public void heartbeat(final long nextDueInterval)
	{
		lastHeartbeat = new Date();
		nextHeartbeatDue = new Date(lastHeartbeat.getTime() + nextDueInterval);

		for (ServiceDescription service : services)
		{
			service.lastHeartbeat = lastHeartbeat;
			service.nextHeartbeatDue = nextHeartbeatDue;
		}
	}

	public void populate(RegistrationRequest request)
	{
		this.applicationName = request.applicationName; // Allow the application to change its name

		for (ServiceDetails service : request.services)
		{
			services.add(new ServiceDescription(applicationId, applicationName, service));
		}
	}
}
