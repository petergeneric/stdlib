package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
import com.peterphi.std.guice.metrics.rest.types.HealthDocument;
import com.peterphi.std.guice.web.rest.service.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

@AuthConstraint(id = "framework-admin", role = "framework-admin")
public class HealthRestServiceImpl implements HealthRestService
{
	private static final String PREFIX = "/com/peterphi/std/guice/metrics/rest/impl/";

	@Inject
	private HealthCheckRegistry registry;

	@Inject
	GuiceCoreTemplater templater;

	@Inject
	MetricSerialiser serialiser;


	@Override
	public HealthDocument get()
	{
		HealthDocument doc = new HealthDocument();

		doc.results = serialiser.serialiseHealthChecks(registry.runHealthChecks());

		return doc;
	}


	@Override
	public String getHTML()
	{
		TemplateCall call = templater.template(PREFIX + "health-index.html");

		call.set("health", get());

		return call.process();
	}
}
