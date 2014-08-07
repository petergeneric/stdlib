package com.peterphi.std.indexservice.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.peterphi.std.indexservice.guice.module.IndexServiceModule;
import org.apache.commons.configuration.Configuration;

import java.util.List;

public class IndexGuiceSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// no action required
	}


	@Override
	public void addModules(List<Module> modules, Configuration config)
	{
		modules.add(new IndexServiceModule());
	}
}
