package com.peterphi.std.guice.web.rest.jaxrs;

import com.peterphi.std.guice.apploader.GuiceApplication;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * A jax-rs REST {@link Application} implementation that returns singletons whih are dynamic proxies bound to objects built by
 * Guice<br />
 * This allows for a vanilla jax rs implementation, albeit at a small runtime performance penalty.
 */
public class GuiceRestApplication extends Application implements GuiceApplication
{
	private GuiceRestApplicationRegistry registry = new GuiceRestApplicationRegistry();

	public GuiceRestApplication()
	{
		GuiceRegistry.register(this, true);
	}

	@Override
	public Set<Object> getSingletons()
	{
		// Clear out any old dynamic proxies and create new ones
		registry.clear();
		registry.registerAll(RestResourceRegistry.getResources());

		Set<Object> singletons = new HashSet<Object>();

		for (GuiceDynamicProxyProvider provider : registry.getDynamicProxyProviders())
		{
			singletons.add(provider.getProxy());
		}

		return singletons;
	}

	@Override
	public void configured()
	{
	}

	@Override
	public void stopping()
	{
	}
}
