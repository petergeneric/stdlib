package com.peterphi.std.indexservice.rest.impl.repo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.indexservice.rest.impl.RegisteredApp;
import com.peterphi.std.indexservice.rest.type.RegistrationRequest;
import com.peterphi.std.indexservice.rest.type.ServiceDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AppRepo
{
	private final Map<String, RegisteredApp> applications = new HashMap<String, RegisteredApp>();

	@Inject
	protected ServiceRepo services;

	/**
	 * Applications expire when they have not checked in for 2 minutes
	 */
	protected long applicationHeartbeatExpire = 120 * 1000;

	/**
	 * Applications should send a heartbeat once a minute
	 */
	protected long applicationHeartbeatRate = 60 * 1000;

	public void reregister(String applicationId, RegistrationRequest request)
	{
		synchronized (applications)
		{
			unregister(applicationId);

			register(applicationId, request);
		}
	}

	public void unregister(String applicationId)
	{
		final RegisteredApp app;

		synchronized (applications)
		{
			app = applications.remove(applicationId);
		}

		if (app != null)
		{
			// Unregister the services
			for (ServiceDescription service : app.services)
			{
				services.remove(service);
			}
		}
	}

	public List<ServiceDescription> getServices(String applicationId)
	{
		final RegisteredApp app;

		synchronized (applications)
		{
			app = applications.get(applicationId);
		}

		if (app != null)
		{
			return app.services;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	public String register(RegistrationRequest request)
	{
		return register(UUID.randomUUID().toString(), request);
	}

	protected String register(String applicationId, RegistrationRequest request)
	{
		final RegisteredApp app = new RegisteredApp();
		app.applicationId = applicationId;

		app.populate(request); // Populate the service list
		app.heartbeat(applicationHeartbeatExpire); // Heartbeat the app and service list

		synchronized (applications)
		{
			if (applications.containsKey(applicationId))
				throw new IllegalArgumentException("Application with id " + applicationId + " already registered");
			else
				applications.put(applicationId, app); // Record the application
		}

		for (ServiceDescription service : app.services)
		{
			services.add(service);
		}

		return applicationId;
	}

	/**
	 * Sends a heartbeat message
	 *
	 * @param applicationId
	 *
	 * @return true if the heartbeat was successful, otherwise false it it was unsuccessful (i.e. the application is not known)
	 */
	public boolean heartbeat(String applicationId)
	{
		final RegisteredApp app;
		synchronized (applications)
		{
			app = applications.get(applicationId);
		}

		if (app != null)
		{
			app.heartbeat(applicationHeartbeatExpire);
		}

		return (app != null);
	}

	public void expire()
	{
		synchronized (applications)
		{
			final List<String> expired = new ArrayList<String>(0);

			final long now = System.currentTimeMillis();

			// Find expired applications
			for (RegisteredApp app : applications.values())
			{
				if (app.nextHeartbeatDue.getTime() < now)
				{
					expired.add(app.applicationId);
				}
			}

			// Unregister the expired applications
			for (String applicationId : expired)
			{
				unregister(applicationId);
			}
		}
	}

	public long getHeartbeatRate()
	{
		return this.applicationHeartbeatRate;
	}

	public List<String> getAllIds()
	{
		synchronized (applications)
		{
			return new ArrayList<String>(applications.keySet());
		}
	}
}
