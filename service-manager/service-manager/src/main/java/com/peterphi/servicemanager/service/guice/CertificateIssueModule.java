package com.peterphi.servicemanager.service.guice;

import com.google.inject.AbstractModule;
import com.microsoft.azure.management.dns.DnsManagementClient;

public class CertificateIssueModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(DnsManagementClient.class).toProvider(AzureDNSClientProvider.class);
	}
}
