package com.mediasmiths.std.guice.testrestclient.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.metrics.module.WebappStatsModule;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class Setup extends AbstractRESTGuiceSetup
{
	@Override
	public void injectorWasCreated(Injector injector)
	{

	}


	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new MyAppModule());
		modules.add(new ThymeleafModule());
		modules.add(new WebappStatsModule());
	}
}
