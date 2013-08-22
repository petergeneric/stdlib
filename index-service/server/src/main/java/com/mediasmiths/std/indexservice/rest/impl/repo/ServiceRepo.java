package com.mediasmiths.std.indexservice.rest.impl.repo;

import com.google.inject.Singleton;
import com.mediasmiths.std.indexservice.rest.type.ServiceDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ServiceRepo
{
	private final Map<String, List<ServiceDescription>> services = new HashMap<String, List<ServiceDescription>>();

	public List<ServiceDescription> findByInterface(String iface)
	{
		synchronized (services)
		{
			final List<ServiceDescription> results = getByInterface(iface);

			if (results.isEmpty())
				return Collections.emptyList();
			else
				return new ArrayList<ServiceDescription>(results);
		}
	}

	private List<ServiceDescription> getByInterface(String iface)
	{
		final List<ServiceDescription> results;
		synchronized (services)
		{
			results = services.get(iface);
		}

		if (results != null)
			return results;
		else
			return Collections.emptyList();
	}

	public void add(ServiceDescription service)
	{
		final String iface = service.details.iface;

		synchronized (services)
		{
			List<ServiceDescription> existing = services.get(iface);

			if (existing == null)
			{
				existing = new ArrayList<ServiceDescription>(1);
				services.put(iface, existing);
			}

			existing.add(service);
		}
	}

	public void remove(ServiceDescription service)
	{
		final String iface = service.details.iface;

		synchronized (services)
		{
			final List<ServiceDescription> results = findByInterface(iface);

			if (!results.isEmpty())
			{
				results.remove(service);

				// If that was the last service we should remove this interface from the map
				if (results.isEmpty())
				{
					services.remove(iface);
				}
			}
		}
	}

	public List<ServiceDescription> getAllServices()
	{
		synchronized (services)
		{
			List<ServiceDescription> results = new ArrayList<ServiceDescription>(128);

			for (List<ServiceDescription> list : services.values())
			{
				results.addAll(list);
			}

			return results;
		}
	}
}
