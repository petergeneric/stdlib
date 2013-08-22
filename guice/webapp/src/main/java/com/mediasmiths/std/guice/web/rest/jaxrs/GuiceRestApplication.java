package com.mediasmiths.std.guice.web.rest.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.mediasmiths.std.guice.apploader.GuiceApplication;
import com.mediasmiths.std.guice.apploader.impl.GuiceRegistry;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

/**
 * A jax-rs REST {@link Application} implementation that returns singletons whih are dynamic proxies bound to objects built by Guice<br />
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
