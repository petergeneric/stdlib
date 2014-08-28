package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
import com.peterphi.std.guice.metrics.rest.types.HealthDocument;
import com.peterphi.std.guice.metrics.role.MetricsServicesModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

public class HealthRestServiceImpl implements HealthRestService
{
	@Inject
	private HealthCheckRegistry registry;


	@Inject
	@Named(MetricsServicesModule.METRICS_UI_THYMELEAF)
	ThymeleafTemplater templater;

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
		TemplateCall call = templater.template("health-index");

		call.set("health", get());

		return call.process();
	}
}
