package com.peterphi.std.guice.testrestclient.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.metrics.module.WebappStatsModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafModule;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class Setup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final Configuration config)
	{
		modules.add(new MyAppModule());
		modules.add(new ThymeleafModule());
		modules.add(new WebappStatsModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
