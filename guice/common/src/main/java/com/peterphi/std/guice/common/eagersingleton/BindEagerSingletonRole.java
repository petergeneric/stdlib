package com.peterphi.std.guice.common.eagersingleton;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BindEagerSingletonRole implements GuiceRole
{
	private static final Logger log = Logger.getLogger(BindEagerSingletonRole.class);


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
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		if (scanner != null)
		{
			final List<Class<?>> classes = scanner.getAnnotatedClasses(EagerSingleton.class);

			if (log.isTraceEnabled())
			{
				log.trace("There are " + classes.size() + " classes annotated with @EagerSingleton");
				for (Class<?> clazz : classes)
					log.trace("Binding eager singleton: " + clazz);
			}

			if (!classes.isEmpty())
				modules.add(new BindEagerSingletonModule(classes));
		}
	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScanner scanner,
	                            final CompositeConfiguration config,
	                            final PropertiesConfiguration overrides,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{

	}
}
