package com.peterphi.servicemanager.service.rest.resource.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.azure.management.compute.PowerState;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import com.peterphi.servicemanager.service.rest.resource.jaxb.AzureExistingVM;
import com.peterphi.std.azure.AzureVMControl;

import java.util.Map;

@Singleton
public class AzureExistingVMProvider
{
	public static final String PROVIDER = "AzureExistingVM";

	@Inject
	AzureVMControl azure;


	public void start(final AzureExistingVM azureVM, final Map<String, String> metadata)
	{
		start(azureVM.id);
	}


	public void stop(final ResourceInstanceEntity instance)
	{
		stop(instance.getProviderInstanceId());
	}


	public void start(final String id)
	{
		azure.requestStartIfStopped(id);
	}


	public void stop(final String id)
	{
		azure.requestStopIfRunning(id);
	}


	public ResourceInstanceState determineState(final ResourceInstanceEntity instance)
	{
		final String id = instance.getProviderInstanceId();
		final PowerState powerState = azure.getPowerState(id);

		final boolean isDiscarding = instance.getState() == ResourceInstanceState.DISCARDING ||
		                             instance.getState() == ResourceInstanceState.TO_DISCARD;
		final boolean isProvisioning = instance.getState() == ResourceInstanceState.PROVISIONING ||
		                               instance.getState() == ResourceInstanceState.TO_PROVISION;

		if (isDiscarding)
		{
			switch (powerState)
			{
				case DEALLOCATED:
					return ResourceInstanceState.DISCARDED;
				case DEALLOCATING:
					return ResourceInstanceState.DISCARDING;
				case RUNNING:
				case STARTING:
					return ResourceInstanceState.TO_DISCARD;
				default:
					throw new IllegalArgumentException("Unknown Azure power state for " + id + ": " + powerState);
			}
		}
		else if (isProvisioning)
		{
			switch (powerState)
			{
				case DEALLOCATED:
				case DEALLOCATING:
					return ResourceInstanceState.TO_PROVISION;
				case RUNNING:
					return ResourceInstanceState.IN_SERVICE;
				case STARTING:
					return ResourceInstanceState.PROVISIONING;
				default:
					throw new IllegalArgumentException("Unknown Azure power state for " + id + ": " + powerState);
			}
		}
		else
		{
			switch (powerState)
			{
				case DEALLOCATED:
					return ResourceInstanceState.DISCARDED;
				case DEALLOCATING:
					return ResourceInstanceState.DISCARDING;
				case RUNNING:
					return ResourceInstanceState.IN_SERVICE;
				case STARTING:
					return ResourceInstanceState.PROVISIONING;
				default:
					throw new IllegalArgumentException("Unknown Azure power state for " + id + ": " + powerState);
			}
		}
	}
}
