package com.peterphi.servicemanager.service.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.rules.daemon.RulesDaemonModule;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class ServiceManagerGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig config)
	{
		modules.add(new ServiceManagerGuiceModule(config));
		modules.add(new RulesDaemonModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
