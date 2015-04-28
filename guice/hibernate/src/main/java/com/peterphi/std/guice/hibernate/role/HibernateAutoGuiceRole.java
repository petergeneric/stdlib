package com.peterphi.std.guice.hibernate.role;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HibernateAutoGuiceRole implements GuiceRole
{
	@Override
	public void adjustConfigurations(final List<Configuration> configs)
	{

		//sets a default value for GuiceProperties.HIBERNATE_ALLOW_HBM2DDL_CREATE if it is not present in the configs already

		boolean hbm2ddlfound = false;
		for (Configuration config : configs)
		{
			if (config.containsKey(GuiceProperties.HIBERNATE_ALLOW_HBM2DDL_CREATE))
			{
				hbm2ddlfound = true;
			}
		}

		if (!hbm2ddlfound)
		{
			//set default value
			configs.get(0).addProperty(GuiceProperties.HIBERNATE_ALLOW_HBM2DDL_CREATE, "false");
		}
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
		// Unless the user has disabled automatic hibernate setup...
		if (scanner != null && config.getBoolean(GuiceProperties.ROLE_HIBERNATE_AUTO, true))
		{
			modules.add(new AutoHibernateModule(scanner, metrics));
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
