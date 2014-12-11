package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.GuiceRole;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Build the specifications for a new guice framework environment
 */
public class GuiceBuilder
{
	private static final Logger log = Logger.getLogger(GuiceBuilder.class);

	private boolean autoLoadProperties = true;
	private boolean autoLoadRoles = true;
	private ClassScannerFactory scannerFactory = null;
	private List<Configuration> configs = new ArrayList<>();
	private List<GuiceRole> roles = new ArrayList<>();
	private GuiceSetup setup;
	private ClassLoader classloader;
	private GuiceRegistry registry;


	public GuiceBuilder()
	{
		this.classloader = Thread.currentThread().getContextClassLoader();

		this.roles.add(new CoreGuiceRole());
	}


	public GuiceBuilder withRegistry(GuiceRegistry registry)
	{
		this.registry = registry;

		return this;
	}


	public GuiceBuilder withScannerFactory(ClassScannerFactory scannerFactory)
	{
		this.scannerFactory = scannerFactory;

		return this;
	}

	
	public GuiceBuilder withNoScannerFactory()
	{
		return withScannerFactory(new ClassScannerFactory());
	}


	public GuiceBuilder withRole(GuiceRole... roles)
	{
		for (GuiceRole role : roles)
			this.roles.add(role);

		return this;
	}


	public GuiceBuilder withAutoLoadRoles(final boolean autoLoadRoles)
	{
		this.autoLoadRoles = autoLoadRoles;

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
				this.configs.addAll(GuiceFactory.loadConfig(classloader, filename));
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


	public GuiceBuilder withSetup(Class<? extends GuiceSetup> clazz)
	{
		final GuiceSetup obj;
		try
		{
			obj = clazz.newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not instantiate class " + clazz, e);
		}

		return withSetup(obj);
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


	/**
	 * Instantiate the guice environment
	 *
	 * @return
	 */
	public Injector build()
	{
		// Create copies of the configurations and roles
		List<Configuration> configs = new ArrayList<>(this.configs);
		List<GuiceRole> roles = new ArrayList<>(this.roles);

		return GuiceFactory.build(this.registry,
		                          this.scannerFactory,
		                          configs,
		                          roles,
		                          this.setup,
		                          this.autoLoadProperties,
		                          this.autoLoadRoles,
		                          this.classloader);
	}


	/**
	 * Construct a GuiceBuilder with sensible defaults for testing
	 *
	 * @return
	 */
	public static GuiceBuilder forTesting()
	{
		return new GuiceBuilder().withAutoLoadRoles(false);
	}


	/**
	 * Construct a GuiceBuilder with sensible defaults for testing. Adds the provided modules (wrapped in a {@link
	 * com.peterphi.std.guice.apploader.BasicSetup} automatically) to the environment that will be constructed
	 *
	 * @param modules
	 *
	 * @return
	 */
	public static GuiceBuilder forTesting(Module... modules)
	{
		return forTesting().withSetup(new BasicSetup(modules));
	}
}
