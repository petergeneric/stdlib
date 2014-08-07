package com.peterphi.std.guice.web.rest.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.apploader.impl.GuiceRegistry;
import com.peterphi.std.guice.common.serviceprops.ConfigurationConverter;
import com.peterphi.std.guice.web.rest.exception.TextWebException;
import org.apache.commons.configuration.Configuration;

import java.io.StringWriter;

/**
 * A helper REST service that
 */
@Singleton
public class GuiceRestCoreServiceImpl implements GuiceRestCoreService
{
	private final long started = System.currentTimeMillis();

	@Inject(optional = true)
	@Named("restutils.show-serviceprops")
	@Doc("If true, then the configuration data for the application will be available for remote inspection (default false). Should be disabled for live systems because this may leak password data.")
	boolean showProperties = false;

	@Inject(optional = true)
	@Named("restutils.allow-restart")
	@Doc("If true, then a restart of the guice environment without involving the servlet container may be attempted (default false). Should be disabled for live systems.")
	boolean allowRestart = false;

	@Inject
	Configuration configuration;


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
		if (showProperties)
		{
			StringWriter sw = new StringWriter();
			ConfigurationConverter.toProperties(configuration).store(sw, "Properties exported for REST request");

			return sw.toString();
		}
		else
		{
			throw new TextWebException(403,
			                           "API display of service.properties has not been permitted. see restutils.show-serviceprops in service.properties");
		}
	}


	@Override
	public String restart() throws Exception
	{
		if (allowRestart)
		{
			GuiceRegistry.restart();

			return "OK, Guice will now restart";
		}
		else
		{
			throw new TextWebException(403,
			                           "API triggering of Guice Restart has not been permitted. see restutils.allow-restart in service.properties");
		}
	}
}
