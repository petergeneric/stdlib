package com.peterphi.std.guice.web.rest.setup;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.ClassScannerFactory;
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
	                     final ClassScannerFactory scannerFactory,
	                     final CompositeConfiguration config,
	                     final PropertiesConfiguration overrides,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		// TODO remove HACK Don't run if we're within a unit test (this is an ugly hack...)
		if (!config.getBoolean(GuiceProperties.UNIT_TEST, false))
		{
			final ClassScanner scanner = scannerFactory.getInstance();

			if (scanner == null)
				throw new IllegalArgumentException("No classpath scanner available, missing scan.packages?");

			if (config.getBoolean(GuiceProperties.ROLE_JAXRS_SERVER_AUTO, true))
			{
				modules.add(new JAXRSAutoRegisterServicesModule(config, scannerFactory));
			}
		}
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scanner,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
