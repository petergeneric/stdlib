package com.peterphi.servicemanager.service.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.rules.daemon.RulesDaemonModule;
import com.peterphi.std.azure.guice.AzureModule;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class ServiceManagerGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig config)
	{
		modules.add(new ServiceManagerGuiceModule(config));

		if (config.getBoolean("rules.enabled", false))
		{
			modules.add(new RulesDaemonModule());
			modules.add(new AzureModule());
		}

		// Resource Provisioning
		modules.add(new ResourceGuiceModule());
		modules.add(new AzureModule()); // TODO only if Azure is enabled
		modules.add(new CertificateIssueModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
