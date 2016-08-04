package com.peterphi.std.guice.web.rest.service.restcore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.rest.exception.TextWebException;

/**
 * A helper REST service that allows basic services; superseded by other core services
 */
@Singleton
@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class GuiceRestCoreServiceImpl implements GuiceRestCoreService
{
	private final long started = System.currentTimeMillis();

	@Reconfigurable
	@Inject(optional = true)
	@Named(GuiceProperties.ALLOW_RESTART)
	boolean allowRestart = false;

	@Inject
	GuiceConfig configuration;

	@Inject
	GuiceRegistry registry;


	@Override
	@AuthConstraint(skip = true)
	public String ping()
	{
		final long now = System.currentTimeMillis();
		final long uptime = now - started;

		return "REST services running for " + uptime + " ms";
	}


	@Override
	public String restart() throws Exception
	{
		if (allowRestart)
		{
			registry.restart();

			return "OK, Guice will now restart";
		}
		else
		{
			throw new TextWebException(403,
			                           "API triggering of Guice Restart has not been permitted. see " +
			                           GuiceProperties.ALLOW_RESTART +
			                           " in service.properties");
		}
	}
}
