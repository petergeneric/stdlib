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
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.shutdown.ShutdownModule;
import com.peterphi.std.guice.config.rest.iface.ConfigRestService;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyData;
import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;
import com.peterphi.std.guice.restclient.resteasy.impl.JAXBContextResolver;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyClientFactoryImpl;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyProxyClientFactoryImpl;
import com.peterphi.std.io.PropertyFile;
import com.peterphi.std.types.SimpleId;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
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
	                      final List<PropertyFile> configs,
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

		GuiceConfig properties = new GuiceConfig();

		for (PropertyFile config : configs)
			properties.setAll(config);

		// Load all the core property files?
		if (autoLoadProperties)
		{
			applyConfigs(classloader, properties);
		}

		// Read the override configuration property to find the override config file
		// Load the override config file and pass that along too.
		PropertyFile overrideFile = load(properties.get(GuiceProperties.OVERRIDE_FILE_PROPERTY));

		// If there are overrides then rebuild the configuration to reflect it
		if (overrideFile != null)
		{
			log.debug("Applying overrides: " + overrideFile.getFile());
			properties.setOverrides(overrideFile.toMap());
		}


		// Set up the class scanner factory (if the scanner property is set and one has not been provided)
		if (scannerFactory == null)
		{
			List<String> packages = properties.getList(GuiceProperties.SCAN_PACKAGES, Collections.emptyList());

			if (packages != null && !packages.isEmpty())
				scannerFactory = new ClassScannerFactory(packages.toArray(new String[packages.size()]));
			else
				throw new IllegalArgumentException("Property " + GuiceProperties.SCAN_PACKAGES + " has not been set!");
		}

		final GuiceSetup setup;
		if (staticSetup == null)
		{
			// Load the Setup property and load the Setup class
			final Class<? extends GuiceSetup> setupClass = getClass(properties, GuiceSetup.class, GuiceProperties.SETUP_PROPERTY);

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

		return createInjector(registry, scannerFactory, properties, setup, roles);
	}


	private static Injector createInjector(GuiceRegistry registry,
	                                       ClassScannerFactory scannerFactory,
	                                       GuiceConfig config,
	                                       GuiceSetup setup,
	                                       List<GuiceRole> roles)
	{
		final long started = System.currentTimeMillis();

		AtomicReference<Injector> injectorRef = new AtomicReference<>();
		List<Module> modules = new ArrayList<>();

		final Stage stage = Stage.valueOf(config.get(GuiceProperties.STAGE_PROPERTY, Stage.DEVELOPMENT.name()));

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
				role.register(stage, scannerFactory, config, setup, modules, injectorRef, metricRegistry);

			// Initialise the Setup class
			setup.registerModules(modules, config);

			if (log.isTraceEnabled())
				log.trace("Creating Injector with modules: " + modules);

			final Injector injector = Guice.createInjector(stage, modules);
			injectorRef.set(injector);
			for (GuiceRole role : roles)
				role.injectorCreated(stage, scannerFactory, config, setup, modules, injectorRef, metricRegistry);

			setup.injectorCreated(injector);

			if (scannerFactory != null)
			{
				final long finished = System.currentTimeMillis();
				final String contextName = config.get(GuiceProperties.SERVLET_CONTEXT_NAME, "(app)");

				log.debug("Injector for " + contextName + " created in " + (finished - started) + " ms");

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


	private static void applyConfigs(final ClassLoader classloader, final GuiceConfig config)
	{
		// Load all the local configs
		for (String configFile : getPropertyFiles(config))
		{
			try
			{
				for (PropertyFile properties : loadConfig(classloader, configFile))
					config.setAll(properties);
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("Error loading configuration URLs from " + configFile, e);
			}
		}

		// Load the network config (if enabled)
		try
		{
			applyNetworkConfiguration(config);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to retrieve configuration from network!", t);
		}
	}


	private static void applyNetworkConfiguration(final GuiceConfig config)
	{
		final String instanceId = SimpleId.alphanumeric(32);

		if (config.get(GuiceProperties.CONFIG_ENDPOINT, null) != null)
		{
			final boolean useMoxy = config.getBoolean(GuiceProperties.MOXY_ENABLED, true);
			final JAXBContextResolver jaxb = new JAXBContextResolver(new JAXBSerialiserFactory(useMoxy));
			final ResteasyClientFactoryImpl clientFactory = new ResteasyClientFactoryImpl(null, null, jaxb);

			try
			{
				final ResteasyProxyClientFactoryImpl proxyFactory = new ResteasyProxyClientFactoryImpl(clientFactory, config);

				final ConfigRestService client = proxyFactory.getClient(ConfigRestService.class);

				// Get the config path to read
				final String path = config.get(GuiceProperties.CONFIG_PATH,
				                               config.get(GuiceProperties.SERVLET_CONTEXT_NAME, "unknown-service"));

				final ConfigPropertyData data = client.read(path, instanceId, null);

				for (ConfigPropertyValue property : data.properties)
				{
					config.set(property.name, property.value);
				}

				// Make the randomly generated instance id available
				config.set(GuiceProperties.CONFIG_INSTANCE_ID, SimpleId.alphanumeric(32));
			}
			finally
			{
				clientFactory.shutdown();
			}
		}
	}


	private static List<String> getPropertyFiles(final GuiceConfig tempConfig)
	{
		final List<String> configFiles = new ArrayList<>();

		configFiles.add("environment.properties");
		configFiles.add("service.properties");

		final String contextName = tempConfig.get(GuiceProperties.SERVLET_CONTEXT_NAME, "").replaceAll("/", "");

		if (!StringUtils.isEmpty(contextName))
			configFiles.add("services/" + contextName + ".properties");

		return configFiles;
	}


	static List<PropertyFile> loadConfig(final ClassLoader loader, String name) throws IOException
	{
		log.trace("Search for config files with name: " + name);
		final List<PropertyFile> configs = new ArrayList<>();
		final Enumeration<URL> urls = loader.getResources(name);

		while (urls.hasMoreElements())
		{
			final URL url = urls.nextElement();

			log.debug("Loading property file: " + url);

			configs.add(new PropertyFile(url));
		}

		return configs;
	}


	@SuppressWarnings("unchecked")
	private static <T> Class<? extends T> getClass(final GuiceConfig configuration, Class<T> base, final String property)
	{
		final String prop = configuration.get(property);

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


	private static PropertyFile load(String propertyFile)
	{
		if (propertyFile == null)
			return null;

		try
		{
			final File file = new File(propertyFile);

			if (file.exists())
				return new PropertyFile(file);
			else
			{
				PropertyFile props = new PropertyFile();
				props.setFile(file);

				return props;
			}
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Failed to load property file: " + propertyFile, e);
		}
	}
}
