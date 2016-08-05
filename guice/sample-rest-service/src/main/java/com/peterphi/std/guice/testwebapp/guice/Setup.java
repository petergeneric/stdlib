package com.peterphi.std.guice.testwebapp.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class Setup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig config)
	{

	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
