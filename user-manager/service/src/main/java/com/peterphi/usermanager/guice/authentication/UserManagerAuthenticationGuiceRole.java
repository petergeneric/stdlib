package com.peterphi.usermanager.guice.authentication;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UserManagerAuthenticationGuiceRole implements GuiceRole
{

	@Override
	public void adjustConfigurations(final List<PropertyFile> list)
	{

	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory classScannerFactory,
	                     final GuiceConfig guiceConfig,
	                     final GuiceSetup guiceSetup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> atomicReference,
	                     final MetricRegistry metricRegistry)
	{
		if (guiceConfig.getBoolean(GuiceProperties.UNIT_TEST, false))
		{
			// Unit test mode, don't bind any UserLogins
		}
		else
		{
			modules.add(new UserLoginModule());
		}
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory classScannerFactory,
	                            final GuiceConfig guiceConfig,
	                            final GuiceSetup guiceSetup,
	                            final List<Module> list,
	                            final AtomicReference<Injector> atomicReference,
	                            final MetricRegistry metricRegistry)
	{

	}
}
