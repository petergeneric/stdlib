package com.peterphi.std.guice.serviceregistry.index;

/**
 * Describes a service which is not automatically exposed by this application (for example, it may be a remote service that this
 * application is responsible for registering with the index service)
 */
public class ManualIndexableService
{
	public final String serviceInterface;
	public final String endpoint;

	public ManualIndexableService(String serviceInterface, String endpoint)
	{
		this.serviceInterface = serviceInterface;
		this.endpoint = endpoint;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
		result = prime * result + ((serviceInterface == null) ? 0 : serviceInterface.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManualIndexableService other = (ManualIndexableService) obj;
		if (endpoint == null)
		{
			if (other.endpoint != null)
				return false;
		}
		else if (!endpoint.equals(other.endpoint))
			return false;
		if (serviceInterface == null)
		{
			if (other.serviceInterface != null)
				return false;
		}
		else if (!serviceInterface.equals(other.serviceInterface))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ManualIndexableService [serviceInterface=" + serviceInterface + ", endpoint=" + endpoint + "]";
	}
}
