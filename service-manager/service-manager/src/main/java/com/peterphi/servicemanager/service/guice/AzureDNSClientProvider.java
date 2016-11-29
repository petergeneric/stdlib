package com.peterphi.servicemanager.service.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.dns.DnsManagementClient;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.io.IOException;
import java.net.URI;

public class AzureDNSClientProvider implements Provider<DnsManagementClient>
{
	@Inject
	Provider<ApplicationTokenCredentials> azureCredentials;

	@Inject
	@Named("azure.dns.subscription-id")
	private String subscriptionId;


	@Override
	public DnsManagementClient get()
	{
		try
		{
			final Configuration config = ManagementConfiguration.configure(null,
			                                                               URI.create("https://management.core.windows.net/"),
			                                                               subscriptionId,
			                                                               azureCredentials.get().getToken());

			return config.create(DnsManagementClient.class);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error building Azure DNS Client", e);
		}
	}
}
