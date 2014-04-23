package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.JAXBModule;
import com.peterphi.std.guice.common.Log4JModule;
import com.peterphi.std.guice.common.ServicePropertiesModule;
import com.peterphi.std.guice.common.converter.PropertiesTypeConversionModule;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleModule;
import com.peterphi.std.guice.common.retry.module.RetryModule;
import com.peterphi.std.guice.common.shutdown.ShutdownModule;
import com.peterphi.std.guice.serviceregistry.ApplicationContextNameRegistry;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for creating fresh Injector instances. This is handled by reading <code>service.properties</code> and parsing the
 * guice.bootstrap.class property which should point at a
 * {@link GuiceSetup} implementation.
 */
public class GuiceInjectorBootstrap
{
	private static final Logger log = Logger.getLogger(GuiceInjectorBootstrap.class);


	public static Injector createInjector()
	{
		final PropertyFile properties = discoverProperties();

		return createInjector(properties);
	}


	private static PropertyFile discoverProperties()
	{
		final String contextName = ApplicationContextNameRegistry.getContextName();

		final List<String> fileNames = new ArrayList<String>();
		fileNames.add("service.properties");
		fileNames.add("environment.properties");

		if (!StringUtils.isEmpty(contextName))
		{
			// Try to load a property file specific to this application
			// Remove any / character from the path
			String propertyFileName = "services/" + contextName.replace("/", "") + ".properties";

			fileNames.add(propertyFileName);
		}

		return loadAllProperties(fileNames);
	}


	private static PropertyFile loadAllProperties(final List<String> files)
	{
		final PropertyFile file = new PropertyFile();

		for (String name : files)
		{
			log.debug("Loading property files with name: " + name);

			final PropertyFile[] props = PropertyFile.findAll(name);

			if (props != null)
				for (PropertyFile prop : props)
					file.merge(prop);
		}

		file.makeReadOnly();

		return file;
	}


	/**
	 * Create an Injector by loading service.properties and then reading the guice.bootstrap.class from it<br />
	 * This is the entrypoint for the GuiceRegistry
	 *
	 * @return
	 */
	public static Injector createInjector(final PropertyFile properties)
	{
		final GuiceSetup setup = getSetup(properties);

		return createInjector(properties, setup);
	}


	/**
	 * Creates an Injector by taking a preloaded service.properties and a pre-constructed GuiceSetup
	 *
	 * @param properties
	 * @param setup
	 *
	 * @return
	 */
	public static Injector createInjector(final GuiceSetup setup)
	{
		final PropertyFile properties = discoverProperties();

		return createInjector(properties, setup);
	}


	/**
	 * Creates an Injector by taking a preloaded service.properties and a pre-constructed GuiceSetup
	 *
	 * @param properties
	 * @param setup
	 *
	 * @return
	 */
	public static Injector createInjector(final PropertyFile properties, final GuiceSetup setup)
	{
		// We need to hold onto this module while creating - if we partially create we may need to shutdown some services
		ShutdownModule shutdown = new ShutdownModule();
		try
		{
			final List<Module> modules = new ArrayList<Module>();

			modules.add(new GuiceLifecycleModule());
			modules.add(shutdown);
			modules.add(new RetryModule());
			modules.add(new JAXBModule(properties));
			modules.add(new PropertiesTypeConversionModule());
			modules.add(new ServicePropertiesModule(properties));
			modules.add(new Log4JModule(properties));

			setup.registerModules(modules, properties);

			final Injector injector = Guice.createInjector(modules);
			setup.injectorCreated(injector);

			return injector;
		}
		catch (RuntimeException e)
		{
			cleanup(shutdown);

			throw e;
		}
		catch (Error e)
		{
			cleanup(shutdown);

			throw e;
		}
	}


	private static GuiceSetup getSetup(PropertyFile properties)
	{
		final Class<?> clazz = properties.getClass("guice.bootstrap.class", null);

		if (clazz == null)
			throw new IllegalArgumentException("Missing guice.bootstrap.class in config: " + properties.getFile());

		try
		{
			return (GuiceSetup) clazz.newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error loading guice.bootstrap.class " + clazz + ": " + e.getMessage(), e);
		}
	}


	private static void cleanup(ShutdownModule shutdown)
	{
		shutdown.shutdown();
	}
}
