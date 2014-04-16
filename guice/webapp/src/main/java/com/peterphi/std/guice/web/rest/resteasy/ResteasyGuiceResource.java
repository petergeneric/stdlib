package com.peterphi.std.guice.web.rest.resteasy;

import com.google.inject.Injector;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Represents a resteasy REST resource that will be retrieved through Guice
 */
class ResteasyGuiceResource implements ResourceFactory
{
	private Injector injector;
	private Class<?> clazz;


	public ResteasyGuiceResource(final Injector injector, Class<?> clazz)
	{
		this.injector = injector;
		this.clazz = clazz;
	}


	@Override
	public void registered(final ResteasyProviderFactory factory)
	{
	}


	@Override
	public Object createResource(final HttpRequest request, final HttpResponse response, final ResteasyProviderFactory factory)
	{
		final Object instance = injector.getInstance(clazz);

		return instance;
	}


	@Override
	public void unregistered()
	{
	}


	@Override
	public Class<?> getScannableClass()
	{
		return clazz;
	}


	@Override
	public void requestFinished(HttpRequest request, HttpResponse response, Object resource)
	{
	}
}
