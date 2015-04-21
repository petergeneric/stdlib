package com.peterphi.std.guice.web.rest.service.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.function.Supplier;

@Singleton
public class GuiceRestLoggingServiceImpl implements GuiceRestLoggingService
{
	/**
	 * The resource prefix
	 */
	private static final String PREFIX = "/com/peterphi/std/guice/web/rest/service/restcore/";

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	URI restEndpoint;

	@Inject
	ConfigurationPropertyRegistry config;

	private String staticConfig;


	@Override


	public String getIndex()
	{
		TemplateCall call = templater.template(PREFIX + "logging.html");

		call.set("log4jPropertiesString", (Supplier<String>) this :: getLog4jPropertiesAsString);

		return call.process();
	}


	private String getLog4jPropertiesAsString()
	{
		if (staticConfig != null)
			return staticConfig;

		final String src = config.get(GuiceProperties.LOG4J_PROPERTIES_FILE).getValue();

		if (StringUtils.equalsIgnoreCase("embedded", src))
		{
			return "# Service has embedded log4j.properties in service properties";
		}
		else if (src == null)
		{
			return "# Service has no log4j.properties defined, so is using log4j defaults";
		}
		else
		{
			final StringWriter sw = new StringWriter(1024);

			try
			{
				final PropertyFile props = load(src);
				props.save(null, sw);
			}
			catch (IOException e)
			{
				throw new RuntimeException("error reading log4j properties into string", e);
			}

			return sw.toString();
		}
	}


	@Override
	public Response loadConfigFile(final String resource)
	{
		reconfigure(load(resource));

		staticConfig = null;

		// Modify the in-memory config to point at the user-specified properties file
		config.get(GuiceProperties.LOG4J_PROPERTIES_FILE).set(resource);

		// Now redirect back to the main logging page
		return Response.seeOther(URI.create(restEndpoint.toString() + "/guice/logging")).build();
	}


	@Override
	public Response loadConfig(final String configuration) throws IOException
	{
		PropertyFile props = new PropertyFile(new StringReader(configuration));

		reconfigure(props);

		staticConfig = configuration;

		// Now redirect back to the main logging page
		return Response.seeOther(URI.create(restEndpoint.toString() + "/guice/logging")).build();
	}


	protected void reconfigure(PropertyFile props)
	{
		//reset any existing log config
		LogManager.resetConfiguration();

		//apply the specified properties
		PropertyConfigurator.configure(props.toProperties());
	}


	protected PropertyFile load(final String resource)
	{
		return PropertyFile.find(resource);
	}
}
