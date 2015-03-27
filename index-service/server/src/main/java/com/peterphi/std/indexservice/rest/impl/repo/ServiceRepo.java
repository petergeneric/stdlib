package com.peterphi.std.indexservice.rest.impl.repo;

import com.google.inject.Singleton;
import com.peterphi.std.indexservice.rest.type.PropertyList;
import com.peterphi.std.indexservice.rest.type.PropertyValue;
import com.peterphi.std.indexservice.rest.type.ServiceDescription;

import java.util.*;

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

    public List<ServiceDescription> findByInterfaceRestrictByProperties(String iface,PropertyList properties) {
        synchronized (services) {
            List<ServiceDescription> interfaceMatches = getByInterface(iface);
            List<ServiceDescription> results = new LinkedList<>(interfaceMatches);
            for(int i = 0; i < interfaceMatches.size();i++) {
                ServiceDescription serviceDescription = interfaceMatches.get(i);
                for (PropertyValue property : properties.properties) {
                    if (!hasPropertyMatch(property.name, property.value, serviceDescription.details.properties)) {
                        results.remove(i);
                        break;
                    }
                }
            }
            return results;
        }
    }

    private boolean hasPropertyMatch(String name, String value, PropertyList properties) {
        for(PropertyValue property : properties.properties) {
            if(property.name.equals(name) && property.value.equals(value)) {
                return true;
            }
        }
        return false;
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