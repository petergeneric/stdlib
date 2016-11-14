package com.peterphi.std.guice.web.rest.service.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.common.logging.Log4JModule;
import com.peterphi.std.guice.common.logging.appender.InMemoryAppender;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import com.peterphi.std.io.PropertyFile;
import org.apache.log4j.spi.LoggingEvent;
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

@Singleton
@AuthConstraint(id = "framework-admin", role = "framework-admin")
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
	GuiceConfig config;


	@Override
	public String getIndex()
	{
		TemplateCall call = templater.template(PREFIX + "logging.html");

		call.set("log4jProperties", getLog4jPropertiesAsString());

		return call.process();
	}


	private String getLog4jPropertiesAsString()
	{
		PropertyFile properties = Log4JModule.getProperties(config);

		if (properties == null)
		{
			return "# Service has no log4j.properties defined, so is using log4j defaults";
		}
		else
		{
			final StringWriter sw = new StringWriter(1024);

			try
			{
				properties.save(null, sw);
			}
			catch (IOException e)
			{
				throw new RuntimeException("error reading log4j properties into string", e);
			}

			return sw.toString();
		}
	}


	@Override
	public Response loadConfig(final String properties)
	{
		// Modify the in-memory config to point at the user-specified properties file
		config.set(GuiceProperties.LOG4J_PROPERTIES_FILE, properties);

		Log4JModule.manualReconfigure(config);

		// Now redirect back to the main logging page
		return Response.seeOther(URI.create(restEndpoint.toString() + "/guice/logging")).build();
	}


	@Override
	public String getRecentLines(final String fromStr)
	{
		final long from = new DateTime(fromStr).getMillis();

		StringWriter sw = new StringWriter(1024);
		PrintWriter pw = new PrintWriter(sw);

		// Visit each event in turn, writing them to the output stream
		InMemoryAppender.visit(event ->
		                       {
			                       if (event.getTimeStamp() > from)
				                       append(pw, event);
		                       });

		return sw.toString();
	}


	public static void append(PrintWriter pw, LoggingEvent event)
	{
		final String loggerName = event.getLoggerName();
		final String category;

		if (loggerName != null && loggerName.contains("."))
			category = loggerName.substring(loggerName.lastIndexOf('.') + 1);
		else
			category = loggerName;

		pw.append(new DateTime(event.getTimeStamp()).toString())
		  .append(" ")
		  .append(event.getLevel().toString())
		  .append(" [")
		  .append(category)
		  .append("] ")
		  .append(event.getRenderedMessage());

		if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() != null)
		{
			pw.append('\n');
			event.getThrowableInformation().getThrowable().printStackTrace(pw);
		}
		pw.append('\n');
	}
}
