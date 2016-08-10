package com.peterphi.usermanager.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.usermanager.guice.module.UserManagerModule;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

public class UserManagerSetup implements GuiceSetup
{
	@Override
	public void registerModules(final List<Module> modules, final GuiceConfig configuration)
	{
		// Authentication handled by the UserManagerAuthenticationGuiceRole role

		modules.add(new UserManagerModule());
	}


	@Override
	public void injectorCreated(final Injector injector)
	{

	}
}
