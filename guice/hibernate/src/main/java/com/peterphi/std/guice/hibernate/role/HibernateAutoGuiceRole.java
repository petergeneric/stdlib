package com.peterphi.std.guice.hibernate.role;

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

public class HibernateAutoGuiceRole implements GuiceRole
{
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
		// Unless the user has disabled automatic hibernate setup...
		if (scanner != null && config.getBoolean(GuiceProperties.ROLE_HIBERNATE_AUTO, true))
		{
			modules.add(new AutoHibernateModule(scanner, metrics));
		}
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
