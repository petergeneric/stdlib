package com.peterphi.std.guice.apploader.impl;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.metrics.CoreMetricsModule;
import com.peterphi.std.guice.common.shutdown.ShutdownModule;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

class GuiceFactory
{
	private static final Logger log = Logger.getLogger(GuiceFactory.class);


	static Injector build(final GuiceRegistry registry,
	                      ClassScannerFactory scannerFactory,
	                      final List<Configuration> configs,
	                      final List<GuiceRole> roles,
	                      final GuiceSetup staticSetup,
	                      final boolean autoLoadProperties,
	                      final boolean autoLoadRoles,
	                      final ClassLoader classloader)
	{

		final ServiceLoader<GuiceRole> loader = ServiceLoader.load(GuiceRole.class);

		// Find additional guice roles from jar files using the Service Provider Interface
		if (autoLoadRoles)
		{
			Iterator<GuiceRole> it = loader.iterator();
			while (it.hasNext())
			{
				final GuiceRole role = it.next();

				log.debug("Discovered guice role: " + role);

				roles.add(role);
			}
		}

		// Allow all GuiceRole implementations to add/remove/reorder configuration sources
		for (GuiceRole role : roles)
		{
			log.debug("Adding requested guice role: " + role);
			role.adjustConfigurations(configs);
		}

		// Load all the core property files?
		if (autoLoadProperties)
		{
			// Create a temporary config (without any overrides) to use for loading other config files
			CompositeConfiguration tempConfig = combine(null, configs);

			configs.addAll(loadConfigs(classloader, tempConfig));
		}

		// Combine all configurations together (assuming no overrides)
		CompositeConfiguration config = combine(null, configs);

		// Read the override configuration property to find the override config file
		// Load the override config file and pass that along too.
		PropertiesConfiguration overrideFile = load(config.getString(GuiceProperties.OVERRIDE_FILE_PROPERTY));

		// If there are overrides then rebuild the configuration to reflect it
		if (overrideFile != null)
		{
			log.debug("Applying overrides: " + overrideFile.getFile());
			config = combine(overrideFile, configs);
		}
		else
		{
			overrideFile = new PropertiesConfiguration();
		}


		// Set up the class scanner factory (if the scanner property is set and one has not been provided)
		if (scannerFactory == null)
		{
			List<Object> packages = config.getList(GuiceProperties.SCAN_PACKAGES, Collections.emptyList());

			if (packages != null && !packages.isEmpty())
				scannerFactory = new ClassScannerFactory(packages.toArray(new String[packages.size()]));
			else
				throw new IllegalArgumentException("Property " + GuiceProperties.SCAN_PACKAGES + " has not been set!");
		}

		final GuiceSetup setup;
		if (staticSetup == null)
		{
			// Load the Setup property and load the Setup class
			final Class<? extends GuiceSetup> setupClass = getClass(config, GuiceSetup.class, GuiceProperties.SETUP_PROPERTY);

			try
			{
				if (setupClass == null)
					throw new IllegalArgumentException("Could not find a setup class!");

				setup = setupClass.newInstance();

				log.debug("Constructed GuiceSetup: " + setupClass);
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new IllegalArgumentException("Error constructing instance of " + setupClass, e);
			}
		}
		else
		{
			log.debug("Using static GuiceSetup: " + staticSetup);
			setup = staticSetup;
		}

		return createInjector(registry, scannerFactory, config, overrideFile, setup, roles);
	}


	private static Injector createInjector(GuiceRegistry registry,
	                                       ClassScannerFactory scannerFactory,
	                                       CompositeConfiguration config,
	                                       PropertiesConfiguration override,
	                                       GuiceSetup setup,
	                                       List<GuiceRole> roles)
	{
		final long started = System.currentTimeMillis();

		AtomicReference<Injector> injectorRef = new AtomicReference<>();
		List<Module> modules = new ArrayList<>();

		final Stage stage = Stage.valueOf(config.getString(GuiceProperties.STAGE_PROPERTY, Stage.DEVELOPMENT.name()));

		// Set up the shutdown module
		ShutdownModule shutdown = new ShutdownModule();

		final MetricRegistry metricRegistry = CoreMetricsModule.buildRegistry();

		try
		{
			// Hold a strong reference to the ClassScanner instance to help the JVM not garbage collect it during startup
			// N.B. we don't actually do anything with the scanner in this method (other than read metrics)
			final ClassScanner scanner = scannerFactory.getInstance();

			modules.add(shutdown);

			if (registry != null)
				modules.add(new GuiceRegistryModule(registry));

			// Initialise all the roles
			for (GuiceRole role : roles)
				role.register(stage, scannerFactory, config, override, setup, modules, injectorRef, metricRegistry);

			// Initialise the Setup class
			setup.registerModules(modules, config);

			if (log.isTraceEnabled())
				log.trace("Creating Injector with modules: " + modules);

			final Injector injector = Guice.createInjector(stage, modules);
			injectorRef.set(injector);
			for (GuiceRole role : roles)
				role.injectorCreated(stage, scannerFactory, config, override, setup, modules, injectorRef, metricRegistry);

			setup.injectorCreated(injector);

			if (scannerFactory != null)
			{
				final long finished = System.currentTimeMillis();
				final String contextName = config.getString(GuiceProperties.SERVLET_CONTEXT_NAME, "(app)");

				log.debug("Injector for " +
				          contextName +
				          " created in " +
				          (finished - started) + " ms");

				if (scanner != null)
					log.debug("Class scanner stats: insts=" +
					          scannerFactory.getMetricNewInstanceCount() +
					          " cached createTime=" +
					          scanner.getConstructionTime() +
					          ", scanTime=" +
					          scanner.getSearchTime());
			}

			return injector;
		}
		catch (Throwable t)
		{
			log.error("Error creating injector", t);

			shutdown.shutdown();

			throw t;
		}
	}


	private static List<Configuration> loadConfigs(final ClassLoader classloader, final CompositeConfiguration tempConfig)
	{
		List<Configuration> configs = new ArrayList<>();

		// Load all the configs
		for (String configFile : getPropertyFiles(tempConfig))
		{
			try
			{
				configs.addAll(loadConfig(classloader, configFile));
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("Error loading configuration URLs from " + configFile, e);
			}
		}

		return configs;
	}


	private static List<String> getPropertyFiles(final CompositeConfiguration tempConfig)
	{
		final List<String> configFiles = new ArrayList<>();

		configFiles.add("environment.properties");
		configFiles.add("service.properties");

		final String contextName = tempConfig.getString(GuiceProperties.SERVLET_CONTEXT_NAME, "").replaceAll("/", "");
		if (!StringUtils.isEmpty(contextName))
			configFiles.add("services/" + contextName + ".properties");

		return configFiles;
	}


	static List<Configuration> loadConfig(final ClassLoader loader, String name) throws IOException
	{
		log.trace("Search for config files with name: " + name);
		final List<Configuration> configs = new ArrayList<>();
		final Enumeration<URL> urls = loader.getResources(name);

		while (urls.hasMoreElements())
		{
			final URL url = urls.nextElement();

			log.debug("Loading property file: " + url);

			try
			{
				configs.add(new PropertiesConfiguration(url));
			}
			catch (ConfigurationException e)
			{
				throw new IOException("Error loading config from " + url, e);
			}
		}

		return configs;
	}


	@SuppressWarnings("unchecked")
	private static <T> Class<? extends T> getClass(final Configuration configuration, Class<T> base, final String property)
	{
		final String prop = configuration.getString(property);

		if (StringUtils.isEmpty(prop))
			return null;

		try
		{
			Class<?> clazz = Class.forName(prop);

			if (base.isAssignableFrom(clazz))
				return (Class<? extends T>) clazz; // unchecked cast
			else
				throw new IllegalArgumentException("Error loading class " + clazz + " - is not assignable from " + base);
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Error loading class " + prop + " from config property " + property, e);
		}
	}


	private static PropertiesConfiguration load(String propertyFile)
	{
		if (propertyFile == null)
			return null;

		try
		{
			final File file = new File(propertyFile);

			if (file.exists())
				return new PropertiesConfiguration(file);
			else
			{
				// Empty configuration, allow it to be written again
				PropertiesConfiguration prop = new PropertiesConfiguration();

				prop.setFile(file);

				return prop;
			}
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Failed to load property file: " + propertyFile, e);
		}
	}


	private static CompositeConfiguration combine(Configuration overrides, List<Configuration> configs)
	{
		final CompositeConfiguration config = new CompositeConfiguration();

		if (overrides != null)
			config.addConfiguration(overrides, true);

		for (int i = configs.size() - 1; i >= 0; i--)
			config.addConfiguration(configs.get(i));

		return config;
	}
}
