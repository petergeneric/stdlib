package com.peterphi.servicemanager.service.rest.resource.service;

import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.rest.resource.jaxb.AzureExistingVM;
import com.peterphi.std.NotImplementedException;

import java.util.Map;

@Singleton
public class AzureExistingVMProvider
{
	public static final String PROVIDER = "AzureExistingVM";


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
		throw new NotImplementedException("TODO azure start");
	}


	public void stop(final String id)
	{
		throw new NotImplementedException("TODO azure stop");
	}
}
