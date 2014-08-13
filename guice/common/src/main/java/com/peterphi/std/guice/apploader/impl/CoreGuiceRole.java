package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.JAXBModule;
import com.peterphi.std.guice.common.Log4JModule;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleModule;
import com.peterphi.std.guice.common.retry.module.RetryModule;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistryModule;
import com.peterphi.std.guice.common.serviceprops.ServicePropertiesModule;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class CoreGuiceRole implements GuiceRole
{
	@Override
	public void adjustConfigurations(final List<Configuration> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef)
	{
		modules.add(new ServicePropertiesModule(config, overrides));
		modules.add(new ConfigurationPropertyRegistryModule(config, overrides, injectorRef));
		modules.add(new GuiceLifecycleModule());
		modules.add(new RetryModule());
		modules.add(new JAXBModule(config));
		modules.add(new Log4JModule(config));
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef)
	{

	}
}
