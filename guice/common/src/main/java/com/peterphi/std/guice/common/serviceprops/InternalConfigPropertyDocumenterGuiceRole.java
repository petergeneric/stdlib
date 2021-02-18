package com.peterphi.std.guice.common.serviceprops;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.serviceprops.annotations.GuicePropertyRegistry;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Role responsible for making it possible to read all Guice Properties that are defined globally (e.g. GuiceProperties) or at the
 * Module level. Eventual aim is to make this lazy-loading, but initially it will just be eager-loaded
 */
public class InternalConfigPropertyDocumenterGuiceRole implements GuiceRole
{
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

	}


	@Override
	public void injectorCreated(final Stage stage,
	                            final ClassScannerFactory scannerFactory,
	                            final GuiceConfig config,
	                            final GuiceSetup setup,
	                            final List<Module> modules,
	                            final AtomicReference<Injector> injectorRef,
	                            final MetricRegistry metrics)
	{
		if (config.getBoolean(GuiceProperties.UNIT_TEST, false))
			return; // Skip property indexing if we're in a unit test (performance)

		// TODO make the actual registration lazy
		bindAllGuiceProperties(injectorRef.get().getInstance(ConfigurationPropertyRegistry.class), injectorRef, modules, setup);
	}


	/**
	 * Create fake bindings for all the properties in {@link com.peterphi.std.guice.apploader.GuiceProperties}
	 *
	 * @param registry
	 * @param injector
	 */
	private static void bindAllGuiceProperties(ConfigurationPropertyRegistry registry,
	                                           AtomicReference<Injector> injector,
	                                           List<Module> modules,
	                                           GuiceSetup setup)
	{
		final Set<Class> classes = new HashSet<>(modules.size() + 5);
		{
			classes.add(GuiceProperties.class);

			// Search all classes
			addClassRef(classes, setup.getClass());

			// Search all modules
			for (Module module : modules)
			{
				addClassRef(classes, module.getClass());
			}
		}

		// Find all "public static final String ..." fields in the referenced classes with a @Doc annotation
		for (Class clazz : classes)
		{
			for (Field field : clazz.getFields())
			{
				if (Modifier.isStatic(field.getModifiers()) &&
				    Modifier.isPublic(field.getModifiers()) &&
				    Modifier.isFinal(field.getModifiers()) &&
				    field.getType().equals(String.class) &&
				    field.isAnnotationPresent(Doc.class))
				{
					try
					{
						final String propertyName = String.valueOf(field.get(null));

						registry.register(clazz, injector, propertyName, String.class, field, true);
					}
					catch (Exception e)
					{
						throw new IllegalArgumentException("Error trying to process GuiceProperties." + field.getName(), e);
					}
				}
			}
		}
	}


	private static boolean addClassRef(Set<Class> classes, Class<?> clazz)
	{
		final boolean added = classes.add(clazz);

		if (added)
		{
			final GuicePropertyRegistry val = clazz.getAnnotation(GuicePropertyRegistry.class);

			if (val != null)
			{
				if (val.ref().length > 0)
				{
					for (Class ref : val.ref())
					{
						addClassRef(classes, ref);
					}
				}
			}
		}

		return added;
	}
}
