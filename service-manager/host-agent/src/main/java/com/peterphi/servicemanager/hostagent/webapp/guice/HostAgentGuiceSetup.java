package com.peterphi.servicemanager.hostagent.webapp.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class HostAgentGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig config)
	{
		modules.add(new HostAgentModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
