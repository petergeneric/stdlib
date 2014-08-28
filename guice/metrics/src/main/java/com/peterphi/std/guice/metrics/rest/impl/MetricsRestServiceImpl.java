package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.types.MetricsDocument;
import com.peterphi.std.guice.metrics.role.MetricsServicesModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

import java.util.SortedMap;
import java.util.TreeMap;

@Singleton
public class MetricsRestServiceImpl implements MetricsRestService
{
	@Inject
	MetricRegistry registry;

	@Inject(optional = true)
	@Named(GuiceProperties.METRICS_JAXRS_SHOW_SAMPLES)
	boolean showSamples = false;

	@Inject
	@Named(MetricsServicesModule.METRICS_UI_THYMELEAF)
	ThymeleafTemplater templater;


	@Inject
	MetricSerialiser serialiser;


	@Override
	public MetricsDocument getMetrics()
	{
		MetricsDocument doc = new MetricsDocument();

		doc.counters = serialiser.serialiseCounters(registry.getCounters());
		doc.gauges = serialiser.serialiseGauges(registry.getGauges());
		doc.histograms = serialiser.serialiseHistograms(registry.getHistograms());
		doc.meters = serialiser.serialiseMeters(registry.getMeters());

		return doc;
	}


	@Override
	public String getIndex()
	{
		TemplateCall call = templater.template("index");

		call.set("gauges", registry.getGauges().entrySet());
		call.set("counters", this.<Counting>combine(registry.getCounters(),
		                                            registry.getMeters(),
		                                            registry.getTimers(),
		                                            registry.getHistograms()).entrySet());
		call.set("meters", this.<Metered>combine(registry.getMeters(), registry.getTimers()).entrySet());
		call.set("histograms", this.<Sampling>combine(registry.getHistograms(), registry.getTimers()).entrySet());

		return call.process();
	}


	private <T> SortedMap<String, T> combine(SortedMap<String, ? extends T>... collections)
	{
		SortedMap<String, T> map = new TreeMap<>();

		for (SortedMap<String, ? extends T> collection : collections)
			map.putAll(collection);

		return map;
	}
}
