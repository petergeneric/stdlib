package com.peterphi.std.guice.apploader.impl;

import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.GuiceSetup;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.log4j.Logger;

/**
 * Responsible for creating fresh Injector instances. This is handled by reading <code>service.properties</code> and parsing the
 * {@link GuiceBuilder#SETUP_PROPERTY} property which should point at a
 * {@link GuiceSetup} implementation.
 *
 * @deprecated use GuiceBuilder directly instead. This class will be removed in a future release.
 */
@Deprecated
public class GuiceInjectorBootstrap
{
	private static final Logger log = Logger.getLogger(GuiceInjectorBootstrap.class);


	public static Injector createInjector()
	{
		return new GuiceBuilder().build();
	}


	/**
	 * Create an Injector by loading service.properties and then reading the guice.bootstrap.class from it<br />
	 * This is the entrypoint for the GuiceRegistry
	 *
	 * @return
	 */
	public static Injector createInjector(final PropertyFile properties)
	{
		return createInjector(properties, null);
	}


	/**
	 * Creates an Injector by taking a pre-constructed GuiceSetup
	 *
	 * @param setup
	 *
	 * @return
	 */
	public static Injector createInjector(final GuiceSetup setup)
	{
		return new GuiceBuilder().withSetup(setup).build();
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
		return createInjector(new MapConfiguration(properties.toProperties()), setup);
	}


	/**
	 * Creates an Injector by taking a preloaded service.properties and a pre-constructed GuiceSetup
	 *
	 * @param properties
	 * @param setup
	 *
	 * @return
	 */
	public static Injector createInjector(final Configuration configuration, final GuiceSetup setup)
	{
		return new GuiceBuilder().withConfig(configuration).withSetup(setup).build();
	}
}
