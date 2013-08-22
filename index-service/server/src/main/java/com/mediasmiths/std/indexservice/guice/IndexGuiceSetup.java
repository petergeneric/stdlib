package com.mediasmiths.std.indexservice.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.indexservice.guice.module.IndexServiceModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class IndexGuiceSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// no action required
	}

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new IndexServiceModule());
	}

}
