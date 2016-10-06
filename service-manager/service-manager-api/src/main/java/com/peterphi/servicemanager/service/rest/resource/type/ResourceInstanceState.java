package com.peterphi.servicemanager.service.rest.resource.type;

public enum ResourceInstanceState
{
	/**
	 * Provider plugin code should run to provision this resource
	 */
	TO_PROVISION,
	/**
	 * Provider plugin code is in the process of provisioning this resource
	 */
	PROVISIONING,
	/**
	 * This resource has been fully provisioned but has not yet been brought into service
	 */
	NOT_IN_SERVICE,
	/**
	 * This resource is in service
	 */
	IN_SERVICE,
	/**
	 * Provider plugin code should run to discard this resource
	 */
	TO_DISCARD,
	/**
	 * Provider plugin code is running to discard this resource
	 */
	DISCARDING,
	/**
	 * This resource has been discarded
	 */
	DISCARDED,
	/**
	 * Resource is in an unexpected/error state
	 */
	ERROR;


	public boolean isProvisioned()
	{
		switch (this)
		{
			case DISCARDED:
				return false;
			default:
				return true;
		}
	}


	public boolean mayDiscard()
	{
		switch (this)
		{
			case TO_PROVISION:
			case PROVISIONING:
			case DISCARDING:
			case TO_DISCARD:
			case DISCARDED:
				return false;
			default:
				return true;
		}
	}
}
