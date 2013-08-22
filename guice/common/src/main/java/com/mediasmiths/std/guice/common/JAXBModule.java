package com.mediasmiths.std.guice.common;

import com.google.inject.AbstractModule;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiserFactory;

/**
 * Exposes JAXBSerialiserFactory as a Singleton, optionally forcing the use of MOXy with the guice.jaxb.moxy config value
 */
public class JAXBModule extends AbstractModule
{
	private final PropertyFile config;

	public JAXBModule(PropertyFile config)
	{
		this.config = config;
	}

	@Override
	protected void configure()
	{
		final boolean useMoxy = config.getBoolean("guice.jaxb.moxy", true);

		bind(JAXBSerialiserFactory.class).toInstance(new JAXBSerialiserFactory(useMoxy));
	}
}
