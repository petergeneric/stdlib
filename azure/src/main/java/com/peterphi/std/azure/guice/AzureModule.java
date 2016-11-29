package com.peterphi.std.azure.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.microsoft.azure.Azure;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Created by bmcleod on 05/09/2016.
 */
public class AzureModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(ApplicationTokenCredentials.class).toProvider(AzureCredentialsProvider.class);
		bind(ServiceClientCredentials.class).to(ApplicationTokenCredentials.class);
		bind(Azure.class).toProvider(AzureProvider.class);
	}


	@Provides
	@Inject
	public VirtualMachines provideVirtualMachinesManagement(Azure azure)
	{
		return azure.virtualMachines();
	}
}
