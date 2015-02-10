package com.peterphi.std.guice.common;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.commons.configuration.Configuration;

/**
 * Exposes JAXBSerialiserFactory as a Singleton, optionally forcing the use of MOXy with the guice.jaxb.moxy config value
 */
public class JAXBModule extends AbstractModule
{
	private final Configuration config;


	public JAXBModule(Configuration config)
	{
		this.config = config;
	}


	@Override
	protected void configure()
	{
		final boolean useMoxy = config.getBoolean(GuiceProperties.MOXY_ENABLED, true);

		bind(JAXBSerialiserFactory.class).toInstance(new JAXBSerialiserFactory(useMoxy));
	}
}
