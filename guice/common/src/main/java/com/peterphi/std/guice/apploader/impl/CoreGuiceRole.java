package com.peterphi.std.guice.apploader.impl;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.JAXBModule;
import com.peterphi.std.guice.common.Log4JModule;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleModule;
import com.peterphi.std.guice.common.metrics.CoreMetricsModule;
import com.peterphi.std.guice.common.retry.module.RetryModule;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistryModule;
import com.peterphi.std.guice.common.serviceprops.ServicePropertiesModule;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class CoreGuiceRole implements GuiceRole
{
	private static final Logger log = Logger.getLogger(CoreGuiceRole.class);


	@Override
	public void adjustConfigurations(final List<Configuration> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scanner,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		modules.add(new ServicePropertiesModule(config, overrides));
		modules.add(new ConfigurationPropertyRegistryModule(config, overrides, injectorRef));
		modules.add(new GuiceLifecycleModule());
		modules.add(new CoreMetricsModule(metrics));
		modules.add(new RetryModule(metrics));
		modules.add(new JAXBModule(config));
		modules.add(new Log4JModule(config, metrics));
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scanner,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            MetricRegistry metrics)
	{

	}
}
