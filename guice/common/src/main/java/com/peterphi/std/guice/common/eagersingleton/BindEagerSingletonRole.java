package com.peterphi.std.guice.common.eagersingleton;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.eagersingleton.annotations.EagerSingleton;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BindEagerSingletonRole implements GuiceRole
{
	private static final Logger log = Logger.getLogger(BindEagerSingletonRole.class);


	@Override
	public void adjustConfigurations(final List<PropertyFile> configs)
	{
	}


	@Override
	public void register(final Stage stage,
	                     final ClassScannerFactory scannerFactory,
	                     final GuiceConfig config,
	                     final GuiceSetup setup,
	                     final List<Module> modules,
	                     final AtomicReference<Injector> injectorRef,
	                     final MetricRegistry metrics)
	{
		final ClassScanner scanner = scannerFactory.getInstance();

		if (scanner != null)
		{
			final List<Class<?>> classes = scanner.getAnnotatedClasses(EagerSingleton.class);
			final boolean isTestEnvironment = config.getBoolean(GuiceProperties.UNIT_TEST, false);

			Iterator<Class<?>> it = classes.iterator();

			while (it.hasNext())
			{
				final Class<?> clazz = it.next();

				if (isTestEnvironment)
				{
					final EagerSingleton annotation = clazz.getAnnotation(EagerSingleton.class);

					// We're in a test environment but inTests isn't true, so we should ignore this annotation
					if (!annotation.inTests())
					{
						log.trace("Ignoring eager singleton with inTests=false: " + clazz);

						it.remove();
						continue;
					}
				}

				log.trace("Binding eager singleton: " + clazz);
			}

			if (!classes.isEmpty())
				modules.add(new BindEagerSingletonModule(classes));
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
