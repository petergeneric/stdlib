package com.peterphi.std.guice.web.rest.jaxrs;

import java.lang.reflect.Method;

import com.peterphi.std.guice.apploader.impl.GuiceRegistry;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

class GuiceDynamicProxyProvider implements MethodHandler
{
	private final Class<?> clazz;
	private final ProxyFactory proxyFactory = new ProxyFactory();

	private Object proxyInstance;

	public GuiceDynamicProxyProvider(Class<?> interfaceType)
	{
		this.clazz = interfaceType;

		proxyFactory.setInterfaces(new Class[] { clazz });
	}

	public synchronized Object getProxy()
	{
		if (proxyInstance == null)
			proxyInstance = createProxy();

		return proxyInstance;
	}

	private Object createProxy()
	{
		try
		{
			return proxyFactory.create(new Class[] {}, new Object[] {}, this);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error creating Proxy for " + clazz + ": " + e.getMessage(), e);
		}
	}

	/**
	 * A MethodHandler that proxies the Method invocation through to a Guice-acquired instance
	 */
	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
	{
		// Get an instance of the implementing class via Guice
		final Object instance = GuiceRegistry.getInjector().getInstance(clazz);

		return thisMethod.invoke(instance, args);
	}
}
