package com.peterphi.std.indexservice.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.indexservice.guice.module.IndexServiceModule;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class IndexGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final Configuration config)
	{
		modules.add(new IndexServiceModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}