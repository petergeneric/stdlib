package com.peterphi.std.guice.web.rest.setup;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AutoJAXRSBindingGuiceRole implements GuiceRole
{
	@Override
	public void adjustConfigurations(final List<Configuration> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScanner scanner,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef)
	{
		// TODO should we also confirm we're in a servlet environment?

		if (scanner != null && config.getBoolean(GuiceProperties.ROLE_JAXRS_SERVER_AUTO, true))
		{
			modules.add(new JAXRSAutoRegisterServicesModule(scanner));
		}
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScanner scanner,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef)
	{

	}
}
