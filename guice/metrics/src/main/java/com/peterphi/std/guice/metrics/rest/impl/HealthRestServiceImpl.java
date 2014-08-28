package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
import com.peterphi.std.guice.metrics.role.MetricsServicesModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

import java.io.StringWriter;
import java.util.Map;
import java.util.SortedMap;

public class HealthRestServiceImpl implements HealthRestService
{
	@Inject
	private HealthCheckRegistry registry;

	@Inject
	@Named(MetricsServicesModule.METRICS_UI_THYMELEAF)
	ThymeleafTemplater templater;


	private final ObjectMapper mapper = new ObjectMapper().registerModule(new HealthCheckModule());


	@Override
	public String get()
	{
		try
		{
			final boolean pretty = true;
			final ObjectWriter ow;
			final StringWriter sw = new StringWriter();

			if (pretty)
			{
				ow = mapper.writerWithDefaultPrettyPrinter();
			}
			else
			{
				ow = mapper.writer();
			}

			for (Map.Entry<String, HealthCheck.Result> entry : registry.runHealthChecks().entrySet())
			{
				ow.writeValue(sw, entry);
			}

			ow.writeValue(sw, registry);
			return sw.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getHTML()
	{
		TemplateCall call = templater.template("health-index");

		SortedMap<String, HealthCheck.Result> results = registry.runHealthChecks();

		call.set("health", results);

		return call.process();
	}
}
