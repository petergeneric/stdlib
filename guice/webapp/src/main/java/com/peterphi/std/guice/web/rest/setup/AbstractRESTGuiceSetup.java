package com.peterphi.std.guice.web.rest.setup;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;

import java.util.List;

/**
 * Abstract GuiceSetup implementation that registers standard functionality we offer to REST service modules
 *
 * @deprecated no longer required, extend GuiceSetup directly instead. This class will be removed in a future release.
 */
@Deprecated
public abstract class AbstractRESTGuiceSetup implements GuiceSetup
{
	@Override
	public void registerModules(List<Module> modules, GuiceConfig config)
	{
		addModules(modules, config);
	}


	public abstract void addModules(List<Module> modules, GuiceConfig config);


	@Override
	public final void injectorCreated(Injector injector)
	{
		injectorWasCreated(injector);
	}


	public abstract void injectorWasCreated(Injector injector);
}
