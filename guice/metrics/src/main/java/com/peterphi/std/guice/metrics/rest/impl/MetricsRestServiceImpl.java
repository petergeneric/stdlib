package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.lifecycle.GuiceLifecycleListener;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.role.MetricsServicesModule;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

import java.io.StringWriter;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class MetricsRestServiceImpl implements MetricsRestService, GuiceLifecycleListener
{
	@Inject
	MetricRegistry registry;

	private ObjectMapper mapper;

	@Inject(optional = true)
	@Named(GuiceProperties.METRICS_JAXRS_SHOW_SAMPLES)
	boolean showSamples = false;

	@Inject
	@Named(MetricsServicesModule.METRICS_UI_THYMELEAF)
	ThymeleafTemplater templater;


	@Override
	public String getMetrics()
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

			ow.writeValue(sw, registry);
			return sw.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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


	@Override
	public void postConstruct()
	{
		final TimeUnit rateUnit = TimeUnit.SECONDS;
		final TimeUnit durationUnit = TimeUnit.SECONDS;

		this.mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit, durationUnit, showSamples));
	}
}
