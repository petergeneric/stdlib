package com.peterphi.std.azure.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Created by bmcleod on 05/09/2016.
 */
public class ServiceClientCredentialsProvider implements Provider<ServiceClientCredentials>
{

	@Inject
	@Named("azure.app.client-id")
	private String clientId;

	@Inject
	@Named("azure.app.tenant-id")
	private String tenantId;

	@Inject
	@Named("azure.app.secret")
	private String key;

	@Inject(optional = true)
	@Named("azure.environment")
	private String azureEnvironmentName = "AZURE";


	@Override
	public ServiceClientCredentials get()
	{
		ApplicationTokenCredentials r = new ApplicationTokenCredentials(clientId, tenantId, key, getEnvironment());
		return r;
	}


	private AzureEnvironment getEnvironment()
	{
		switch (azureEnvironmentName)
		{
			case "AZURE":
				return AzureEnvironment.AZURE;
			case "AZURE_CHINA":
				return AzureEnvironment.AZURE_CHINA;
			case "AZURE_GERMANY":
				return AzureEnvironment.AZURE_GERMANY;
			case "AZURE_US_GOVERNMENT":
				return AzureEnvironment.AZURE_US_GOVERNMENT;
		}

		throw new IllegalArgumentException("Invalid setting for azure.environment " + azureEnvironmentName);
	}
}
