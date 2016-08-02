package com.peterphi.configuration.service.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class ConfigGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final Configuration config)
	{
		modules.add(new ConfigGuiceModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
