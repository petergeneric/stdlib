package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.shutdown.ShutdownModule;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GuiceBuilder
{
	private static final Logger log = Logger.getLogger(GuiceBuilder.class);

	private boolean autoLoadProperties = true;
	private List<Configuration> configs = new ArrayList<>();
	private List<GuiceRole> roles = new ArrayList<>();
	private GuiceSetup setup;
	private ClassLoader classloader;
	private GuiceRegistry registry;


	public GuiceBuilder()
	{
		this(null);
	}


	public GuiceBuilder(GuiceRegistry registry)
	{
		this.classloader = this.getClass().getClassLoader();

		this.roles.add(new CoreGuiceRole());
	}


	public GuiceBuilder withRegistry(GuiceRegistry registry)
	{
		this.registry = registry;

		return this;
	}


	public GuiceBuilder withRole(GuiceRole... roles)
	{
		for (GuiceRole role : roles)
			this.roles.add(role);

		return this;
	}


	public GuiceBuilder withConfig(PropertyFile... props)
	{
		for (PropertyFile prop : props)
			this.configs.add(new MapConfiguration(prop.toProperties()));

		return this;
	}


	public GuiceBuilder withConfig(String... filenames)
	{
		for (String filename : filenames)
		{
			try
			{
				this.configs.addAll(loadConfig(classloader, filename));
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("Error loading property file: " + filename);
			}
		}

		return this;
	}


	public GuiceBuilder withConfig(Configuration... configs)
	{
		for (Configuration config : configs)
			this.configs.add(config);

		return this;
	}


	public GuiceBuilder withSetup(GuiceSetup setup)
	{
		this.setup = setup;

		return this;
	}


	public GuiceBuilder withClassloader(ClassLoader classloader)
	{
		this.classloader = classloader;

		return this;
	}


	public GuiceBuilder withAutoLoadProperties(final boolean autoLoadProperties)
	{
		this.autoLoadProperties = autoLoadProperties;

		return this;
	}


	public boolean isAutoLoadProperties()
	{
		return autoLoadProperties;
	}


	public Injector build()
	{
		// Create copies of the configurations and roles
		List<Configuration> configs = new ArrayList<>(this.configs);
		List<GuiceRole> roles = new ArrayList<>(this.roles);

		return build(this.registry, configs, roles, this.setup, this.autoLoadProperties, this.classloader);
	}


	private static Injector build(final GuiceRegistry registry,
	                              final List<Configuration> configs,
	                              final List<GuiceRole> roles,
	                              final GuiceSetup staticSetup,
	                              final boolean autoLoadProperties,
	                              final ClassLoader classloader)
	{
		// TODO find additional guice roles from jar files?

		// Allow all GuiceRole implementations to add/remove/reorder configuration sources
		for (GuiceRole role : roles)
			role.adjustConfigurations(configs);

		// Load all the core property files?
		if (autoLoadProperties)
		{
			// Create a temporary config (without any overrides) to use for loading other config files
			CompositeConfiguration tempConfig = combine(configs);

			configs.addAll(loadConfigs(classloader, tempConfig));
		}

		// TODO find additional guice roles from config?

		// Combine all configurations together
		CompositeConfiguration config = combine(configs);

		// Read the override configuration property to find the override config file
		// Load the override config file and pass that along too.
		final PropertiesConfiguration overrideFile = getPropertyFile(config, GuiceProperties.OVERRIDE_FILE_PROPERY);

		if (overrideFile != null)
			config.addConfiguration(overrideFile, true);

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
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new IllegalArgumentException("Error constructing instance of " + setupClass, e);
			}
		}
		else
		{
			setup = staticSetup;
		}

		return createInjector(registry, config, overrideFile, setup, roles);
	}


	private static Injector createInjector(final GuiceRegistry registry,
	                                       CompositeConfiguration config,
	                                       PropertiesConfiguration override,
	                                       GuiceSetup setup,
	                                       List<GuiceRole> roles)
	{
		AtomicReference<Injector> injectorRef = new AtomicReference<>();
		List<Module> modules = new ArrayList<Module>();

		final Stage stage = Stage.valueOf(config.getString(GuiceProperties.STAGE_PROPERTY, Stage.DEVELOPMENT.name()));

		// Set up the shutdown module
		ShutdownModule shutdown = new ShutdownModule();

		try
		{
			modules.add(shutdown);

			if (registry != null)
				modules.add(new GuiceRegistryModule(registry));

			// Initialise all the roles
			for (GuiceRole role : roles)
				role.register(stage, config, override, setup, modules, injectorRef);

			// Initialise the Setup class
			setup.registerModules(modules, config);

			final Injector injector = Guice.createInjector(stage, modules);
			injectorRef.set(injector);
			for (GuiceRole role : roles)
				role.injectorCreated(stage, config, override, setup, modules, injectorRef);

			setup.injectorCreated(injector);

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


	private static List<Configuration> loadConfig(final ClassLoader loader, String name) throws IOException
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


	private static PropertiesConfiguration getPropertyFile(final Configuration configuration, final String property)
	{
		final String overrideFileStr = configuration.getString(GuiceProperties.OVERRIDE_FILE_PROPERY);

		if (!StringUtils.isEmpty(overrideFileStr))
			return load(overrideFileStr);
		else
			return null;
	}


	private static PropertiesConfiguration load(String propertyFile)
	{
		if (propertyFile == null)
			return null;

		try
		{
			return new PropertiesConfiguration(propertyFile);
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Failed to load property file: " + propertyFile, e);
		}
	}


	private static CompositeConfiguration combine(List<Configuration> configs)
	{
		CompositeConfiguration config = new CompositeConfiguration();

		for (int i = configs.size() - 1; i >= 0; i--)
			config.addConfiguration(configs.get(i));

		return config;
	}
}
