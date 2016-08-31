package com.peterphi.configuration.service.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class ConfigGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig config)
	{
		modules.add(new ConfigGuiceModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
