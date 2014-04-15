package com.peterphi.std.guice.web.rest.jaxrs.converter;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.restclient.converter.JAXRSJodaConverters;
import com.peterphi.std.guice.web.rest.resteasy.ResteasyProviderRegistry;

/**
 * Registers JAX-RS converters for commonly-used types (e.g. Joda Time)
 */
public class JAXRSJodaConverterModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		for (Object converter : JAXRSJodaConverters.getProviderSingletons())
			ResteasyProviderRegistry.registerSingleton(converter);
	}
}
