package com.peterphi.std.guice.web.rest.jaxrs;

import com.peterphi.std.guice.serviceregistry.rest.RestResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class GuiceRestApplicationRegistry
{
	private final Map<RestResource, GuiceDynamicProxyProvider> resources = new HashMap<RestResource, GuiceDynamicProxyProvider>();

	public GuiceRestApplicationRegistry()
	{
	}

	public void registerAll(Collection<RestResource> resources)
	{
		for (RestResource resource : resources)
		{
			register(resource);
		}
	}

	public void register(RestResource resource)
	{
		if (!this.resources.containsKey(resource))
		{
			this.resources.put(resource, new GuiceDynamicProxyProvider(resource.getResourceClass()));
		}
	}

	public void clear()
	{
		resources.clear();
	}

	public Collection<GuiceDynamicProxyProvider> getDynamicProxyProviders()
	{
		return new ArrayList<GuiceDynamicProxyProvider>(this.resources.values());
	}

}
