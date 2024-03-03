package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
import com.peterphi.std.guice.metrics.rest.types.HealthCheckResult;
import com.peterphi.std.guice.metrics.rest.types.HealthDocument;
import com.peterphi.std.guice.metrics.rest.types.HealthImplication;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class HealthRestServiceImpl implements HealthRestService
{
	private static final String PREFIX = "/com/peterphi/std/guice/metrics/rest/impl/";

	@Inject
	private HealthCheckRegistry registry;

	@Inject
	GuiceCoreTemplater templater;


	@Override
	public HealthDocument get()
	{
		HealthDocument doc = new HealthDocument();

		doc.results = serialiseHealthChecks(registry.runHealthChecks());

		return doc;
	}


	@Override
	public String getHTML()
	{
		TemplateCall call = templater.template(PREFIX + "health-index.html");

		call.set("health", get());

		return call.process();
	}


	private static List<HealthCheckResult> serialiseHealthChecks(final SortedMap<String, HealthCheck.Result> results)
	{
		List<HealthCheckResult> list = new ArrayList<>();

		for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	private static HealthCheckResult serialise(String name, final HealthCheck.Result value)
	{
		final HealthImplication implication = HealthImplication.valueOfByPrefix(name);

		// Discard everything before the first : (unless there is none or the implication is unknown, in which case leave it alone)
		if (implication != null)
		{
			final String[] namebits = name.split(":", 2);
			if (namebits.length == 2)
				name = namebits[1];
		}

		return new HealthCheckResult(name, implication, value.isHealthy(), value.getMessage());
	}
}
