package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;

import java.io.StringWriter;
import java.util.Map;

public class HealthRestServiceImpl implements HealthRestService
{
	@Inject
	private HealthCheckRegistry registry;

	private transient ObjectMapper mapper;

	@Override
	public String get()
	{
		if (this.mapper == null)
		{
			this.mapper = new ObjectMapper().registerModule(new HealthCheckModule());
		}

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
				ow.writeValue(sw,entry);
			}

			ow.writeValue(sw, registry);
			return sw.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
