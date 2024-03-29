package com.peterphi.std.guice.apploader.impl;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.JAXBModule;
import com.peterphi.std.guice.common.cached.module.CacheModule;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleModule;
import com.peterphi.std.guice.common.logging.LoggingModule;
import com.peterphi.std.guice.common.metrics.CoreMetricsModule;
import com.peterphi.std.guice.common.retry.module.RetryModule;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistryModule;
import com.peterphi.std.guice.common.serviceprops.ServicePropertiesModule;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class CoreGuiceRole implements GuiceRole
{
	private static final Logger log = LoggerFactory.getLogger(CoreGuiceRole.class);


	@Override
	public void adjustConfigurations(final List<PropertyFile> configs)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scanner,
	                     final GuiceConfig config,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		modules.add(new ServicePropertiesModule(config));

		// Don't gather all properties if we're in a unit test (performance)
		if (!config.getBoolean(GuiceProperties.UNIT_TEST, false))
			modules.add(new ConfigurationPropertyRegistryModule(config, injectorRef));

		modules.add(new GuiceLifecycleModule());
		modules.add(new CoreMetricsModule(metrics));
		modules.add(new CacheModule(metrics));
		modules.add(new RetryModule(metrics));
		modules.add(new JAXBModule(config));
		modules.add(new LoggingModule(config));
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scanner,
	                            final GuiceConfig config,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
