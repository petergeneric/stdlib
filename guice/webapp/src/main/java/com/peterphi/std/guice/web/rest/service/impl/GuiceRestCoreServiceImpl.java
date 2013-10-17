package com.peterphi.std.guice.web.rest.service.impl;

import java.io.StringWriter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.web.rest.exception.TextWebException;
import com.peterphi.std.io.PropertyFile;

/**
 * A helper REST service that
 */
@Singleton
public class GuiceRestCoreServiceImpl implements GuiceRestCoreService
{
	private final long started = System.currentTimeMillis();

	@Inject
	@Named("service.properties")
	protected PropertyFile properties;

	@Override
	public String ping()
	{
		final long now = System.currentTimeMillis();
		final long uptime = now - started;

		return "REST services running for " + uptime + " ms";
	}

	@Override
	public String properties() throws Exception
	{
		final boolean mayDisplay = properties.getBoolean("restutils.show-serviceprops", false);

		if (mayDisplay)
		{
			StringWriter sw = new StringWriter();
			properties.save(null, sw);

			return sw.toString();
		}
		else
		{
			throw new TextWebException(
					403,
					"API display of service.properties has not been permitted. see restutils.show-serviceprops in service.properties");
		}
	}

	@Override
	public String restart() throws Exception
	{
		final boolean mayRestart = properties.getBoolean("restutils.allow-restart", false);

		if (mayRestart)
		{
			GuiceRegistry.restart();

			return "OK, Guice will now restart";
		}
		else
		{
			throw new TextWebException(
					403,
					"API triggering of Guice Restart has not been permitted. see restutils.allow-restart in service.properties");
		}
	}
}
