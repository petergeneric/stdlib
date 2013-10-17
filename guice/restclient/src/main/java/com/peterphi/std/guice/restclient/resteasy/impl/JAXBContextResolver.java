package com.peterphi.std.guice.restclient.resteasy.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.util.jaxb.JAXBSerialiser;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 * Provides JAX-RS with JAXBContext instances acquired through JAXBSerialiserFactory
 */
@Provider
@Produces("application/xml")
@Singleton
public class JAXBContextResolver implements ContextResolver<JAXBContext>
{
	private final JAXBSerialiserFactory factory;

	@Inject
	public JAXBContextResolver(JAXBSerialiserFactory factory)
	{
		this.factory = factory;
	}

	@Override
	public JAXBContext getContext(Class<?> type)
	{
		final JAXBSerialiser serialiser = factory.getInstance(type);

		return serialiser.getContext();
	}
}
