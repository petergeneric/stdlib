package com.peterphi.std.guice.apploader.impl;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceConstants;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.common.logging.ServiceManagerClientGuiceModule;
import com.peterphi.std.guice.common.logging.appender.ServiceManagerAppender;
import com.peterphi.std.guice.common.metrics.CoreMetricsModule;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.net.NetworkConfigGuiceRole;
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
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

class GuiceFactory
{
	private static final Logger log = Logger.getLogger(GuiceFactory.class);


	/**
	 * Build a guice environment; this is achieved in the following stages:
	 * <ol>
	 * <li>Load GuiceRole implementations using {@link ServiceLoader} Service Provider Interface</li>
	 * <li>Allow all GuiceRole instances to add/remove/change base configuration</li>
	 * <li>Load configuration file resources (e.g. environment.properties)</li>
	 * <li>Load network configuration (if enabled)</li>
	 * <li>Add special GuiceRole for network configuration auto-reload (if network configuration enabled)</li>
	 * <li>Load the override configuration file (if present)</li>
	 * <li>Set up the classpath scanner (using property {@link GuiceProperties#SCAN_PACKAGES})</li>
	 * <li>Instantiate the {@link GuiceSetup} class specified in {@link GuiceProperties#SETUP_PROPERTY}</li>
	 * <li>Hand over the GuiceSetup, Roles, Configuration and Classpath Scanner to {@link #createInjector(GuiceRegistry, *
	 * ClassScannerFactory, GuiceConfig, GuiceSetup, List)}</li>
	 * </ol>
	 *
	 * @param registry
	 * 		(optional) the GuiceRegistry to expose to the Guice environment
	 * @param scannerFactory
	 * 		(optional) classpath scanner to use
	 * @param configs
	 * 		base configurations to use
	 * @param roles
	 * 		base roles to use
	 * @param staticSetup
	 * 		(optional) a {@link GuiceSetup} implementation to use instead of loading the class name from a property
	 * @param autoLoadProperties
	 * 		if true, environment.properties etc. and network configuration will be loaded from disk
	 * @param autoLoadRoles
	 * 		if true, roles will be loaded using the Service Provider Interface
	 * @param classloader
	 * 		the classloader to use when loading environment.properties etc.
	 *
	 * @return
	 */
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

		//  Make sure that the first most basic level of properties is the system environment variables
		configs.add(0, getAllEnvironmentVariables());

		// Allow all GuiceRole implementations to add/remove/reorder configuration sources
		for (GuiceRole role : roles)
		{
			log.debug("Adding requested guice role: " + role);
			role.adjustConfigurations(configs);
		}

		GuiceConfig properties = new GuiceConfig();

		// Generate a random instance ID for this instance of the guice environment
		final String instanceId = SimpleId.alphanumeric(32);

		// Make the randomly generated instance id available to others
		properties.set(GuiceProperties.INSTANCE_ID, instanceId);


		for (PropertyFile config : configs)
			properties.setAll(config);

		// Load all the core property files?
		if (autoLoadProperties)
		{
			applyConfigs(classloader, properties);
		}

		// This is a bit of a hack really, but let's insert the GuiceRole for network config if network config is enabled
		if (hasNetworkConfiguration(properties))
		{
			final NetworkConfigGuiceRole role = new NetworkConfigGuiceRole();

			roles.add(role);
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


	private static PropertyFile getAllEnvironmentVariables()
	{
		PropertyFile props = new PropertyFile();

		for (Map.Entry<String, String> entry : System.getenv().entrySet())
		{
			props.set(entry.getKey().toLowerCase(), entry.getValue());
		}

		return props;
	}


	private static boolean hasNetworkConfiguration(final GuiceConfig properties)
	{
		return StringUtils.equals(properties.get(GuiceProperties.CONFIG_SOURCE, null), GuiceConstants.CONFIG_SOURCE_NETWORK);
	}


	/**
	 * Sets up a Guice Injector; this is achieved in the following stages:
	 * <ol>
	 * <li>Set up the Shutdown Manager</li>
	 * <li>Set up the Metrics Registry</li>
	 * <li>Call {@link GuiceRole#register} on all GuiceRoles - this allows modules supporting core plugin functionality to be set
	 * up </li>
	 * <li>Call {@link GuiceSetup#registerModules} on GuiceSetup to get the application's guice modules - this
	 * allows the application to set up helper modules</li>
	 * <li>Call {@link GuiceRole#injectorCreated} on all GuiceRoles with the newly-created {@link Injector} - this allows plugins
	 * to do one-time
	 * post-construction work that requires an Injector</li>
	 * <li>Call {@link GuiceSetup#injectorCreated} with the newly-created {@link Injector} - this allows the
	 * application
	 * to do one-time post-construction work that requires an Injector</li>
	 * </ol>
	 *
	 * @param registry
	 * 		(optional) the {@link GuiceRegistry} to expose to the guice environment
	 * @param scannerFactory
	 * 		the classpath scanner
	 * @param config
	 * 		the system configuration
	 * @param setup
	 * 		the setup class
	 * @param roles
	 * 		guice roles to use
	 *
	 * @return
	 */
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

		// If a service manager endpoint is specified (and skip isn't set) then set up the service manager client
		if (config.get("service.service-manager.endpoint") != null && !config.getBoolean(GuiceProperties.SERVICE_MANAGER_SKIP,
		                                                                                 false))
		{
			modules.add(new ServiceManagerClientGuiceModule(config, shutdown.getShutdownManager()));
		}
		else
		{
			ServiceManagerAppender.shutdown(); // Don't store logs in memory waiting for the service manager, they will never be picked up
		}

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


	/**
	 * Discover and add to the configuration any properties on disk and on the network
	 *
	 * @param classloader
	 * @param config
	 */
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


	/**
	 * Add to the configuration any properties defined by the network configuration endpoint (if network config is enabled in a
	 * previous property)
	 *
	 * @param config
	 */
	private static void applyNetworkConfiguration(final GuiceConfig config)
	{
		final String configEndpoint = config.get(GuiceProperties.CONFIG_ENDPOINT, null);

		final Boolean configSkip = config.getBoolean(GuiceProperties.CONFIG_SKIP, false);

		if (configEndpoint != null && !configSkip)
		{
			final boolean useMoxy = config.getBoolean(GuiceProperties.MOXY_ENABLED, true);
			final JAXBContextResolver jaxb = new JAXBContextResolver(new JAXBSerialiserFactory(useMoxy));
			final ResteasyClientFactoryImpl clientFactory = new ResteasyClientFactoryImpl(null, null, jaxb);

			try
			{
				final ResteasyProxyClientFactoryImpl proxyFactory = new ResteasyProxyClientFactoryImpl(clientFactory, config);

				final ConfigRestService client = proxyFactory.getClient(ConfigRestService.class);

				// Set up the config path if it's not already defined
				// We set it in the config because otherwise the NetworkConfigReloadDaemon won't be able to load the config
				if (config.get(GuiceProperties.CONFIG_PATH) == null)
				{
					config.set(GuiceProperties.CONFIG_PATH,
					           "services/" + config.get(GuiceProperties.SERVLET_CONTEXT_NAME, "unknown-service"));
				}

				// Get the config path to read
				final String path = config.get(GuiceProperties.CONFIG_PATH);

				final ConfigPropertyData data = client.read(path, config.get(GuiceProperties.INSTANCE_ID), null);

				for (ConfigPropertyValue property : data.properties)
				{
					config.set(property.name, property.value);
				}

				// Let others know that the configuration data is coming from a network source
				config.set(GuiceProperties.CONFIG_SOURCE, GuiceConstants.CONFIG_SOURCE_NETWORK);

				if (data.revision != null)
					config.set(GuiceProperties.CONFIG_REVISION, data.revision);
			}
			finally
			{
				clientFactory.shutdown();
			}
		}
		else
		{
			// Config is not coming from the network
			config.set(GuiceProperties.CONFIG_SOURCE, GuiceConstants.CONFIG_SOURCE_LOCAL);
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
