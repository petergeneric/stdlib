package com.peterphi.std.guice.testrestclient.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.metrics.module.WebappStatsModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafModule;
import com.peterphi.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class Setup extends AbstractRESTGuiceSetup
{
	@Override
	public void injectorWasCreated(Injector injector)
	{

	}


	@Override
	public void addModules(List<Module> modules, Configuration config)
	{
		modules.add(new MyAppModule());
		modules.add(new ThymeleafModule());
		modules.add(new WebappStatsModule());
	}
}
