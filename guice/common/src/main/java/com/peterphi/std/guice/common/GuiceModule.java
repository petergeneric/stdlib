package com.peterphi.std.guice.common;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceFactory;
import com.peterphi.std.guice.common.serviceprops.jaxbref.JAXBResourceProvider;
import com.peterphi.std.guice.common.stringparsing.TimeoutConverter;
import com.peterphi.std.threading.Timeout;

import java.util.function.Consumer;

/**
 * Helper class for modules
 */
public abstract class GuiceModule extends AbstractModule
{
	/**
	 * A reference to the Guice Config environment that is automatically set before {@link #configure()} is called<br />
	 */
	protected GuiceConfig config;

	public void setConfig(GuiceConfig config)
	{
		this.config = config;
	}


	/**
	 * Adds a new binding for the given JAXB <code>type</code> to a config source <code>propertyName</code>. This binding
	 * registers a {@link JAXBResourceProvider} that can auto-reload if the underlying file changes
	 *
	 * @param type         the config type
	 * @param propertyName the GuiceConfig property to read from for the config
	 * @param <T>          type of the config file
	 */
	protected <T> JAXBResourceProvider<T> bindConfigFile(final Class<T> type, final String propertyName)
	{
		return bindConfigFile(type, propertyName, null);
	}


	/**
	 * Adds a new binding for the given JAXB <code>type</code> to a config source <code>propertyName</code>. This binding
	 * registers a {@link JAXBResourceProvider} that can auto-reload if the underlying file changes
	 *
	 * @param type         the config type
	 * @param propertyName the GuiceConfig property to read from for the config
	 * @param onLoad       A Consumer to invoke any time the resource is reloaded (i.e. every time it changes)
	 * @param <T>          type of the config file
	 */
	protected <T> JAXBResourceProvider<T> bindConfigFile(final Class<T> type, final String propertyName, Consumer<T> onLoad)
	{
		final JAXBResourceProvider<T> provider = new JAXBResourceProvider<T>(getProvider(JAXBResourceFactory.class),
		                                                                     getProvider(HealthCheckRegistry.class),
		                                                                     propertyName,
		                                                                     type,
		                                                                     onLoad);

		// Optionally allow the cache validity to be configured
		if (config != null)
		{
			final Timeout validity = TimeoutConverter.doConvert(config.get(propertyName + ".refresh", null));

			if (validity != null)
				provider.setCacheValidity(validity);
		}

		bind(type).toProvider(provider);

		return provider;
	}


	@Override
	protected void install(final Module module)
	{
		// Automatically provide the child module a copy of GuiceConfig if it's a GuiceModule
		if (module instanceof GuiceModule)
			((GuiceModule) module).setConfig(config);

		super.install(module);
	}
}
