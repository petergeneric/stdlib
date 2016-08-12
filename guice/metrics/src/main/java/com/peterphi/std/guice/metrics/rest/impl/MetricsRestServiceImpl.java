package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.types.MetricsCounter;
import com.peterphi.std.guice.metrics.rest.types.MetricsDocument;
import com.peterphi.std.guice.metrics.rest.types.MetricsGauge;
import com.peterphi.std.guice.metrics.rest.types.MetricsHistogram;
import com.peterphi.std.guice.metrics.rest.types.MetricsMeter;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;

import java.util.SortedMap;
import java.util.TreeMap;

public class MetricsRestServiceImpl implements MetricsRestService
{
	private static final String PREFIX = "/com/peterphi/std/guice/metrics/rest/impl/";

	@Inject
	MetricRegistry registry;

	@Inject(optional = true)
	@Named(GuiceProperties.METRICS_JAXRS_SHOW_SAMPLES)
	boolean showSamples = false;

	@Inject
	GuiceCoreTemplater templater;


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
	public String getTextMetrics()
	{
		final MetricsDocument doc = getMetrics();

		StringBuilder sb = new StringBuilder(16 * 1024);

		for (MetricsCounter m : doc.counters)
		{
			sb.append(m.name).append(".count=").append(m.count).append("\n");
		}

		for (MetricsGauge m : doc.gauges)
		{
			sb.append(m.name).append(".value=").append(m.value).append("\n");
		}

		for (MetricsHistogram m : doc.histograms)
		{
			sb.append(m.name).append(".count=").append(m.count).append("\n");
			sb.append(m.name).append(".p50=").append(m.percentile50).append("\n");
			sb.append(m.name).append(".p75=").append(m.percentile75).append("\n");
			sb.append(m.name).append(".p95=").append(m.percentile95).append("\n");
			sb.append(m.name).append(".p98=").append(m.percentile98).append("\n");
			sb.append(m.name).append(".p99=").append(m.percentile99).append("\n");
			sb.append(m.name).append(".p99_9=").append(m.percentile999).append("\n");
			sb.append(m.name).append(".snapshot.max=").append(m.snapshotMax).append("\n");
			sb.append(m.name).append(".snapshot.mean=").append(m.snapshotMean).append("\n");
			sb.append(m.name).append(".snapshot.min=").append(m.snapshotMin).append("\n");
			sb.append(m.name).append(".snapshot.size=").append(m.snapshotSize).append("\n");
			sb.append(m.name).append(".snapshot.stddev=").append(m.snapshotStdDev).append("\n");
		}

		for (MetricsMeter m : doc.meters)
		{
			sb.append(m.name).append(".count=").append(m.count).append("\n");
			sb.append(m.name).append(".rate.15m=").append(m.rate15m).append("\n");
			sb.append(m.name).append(".rate.5m=").append(m.rate5m).append("\n");
			sb.append(m.name).append(".rate.1m=").append(m.rate1m).append("\n");
			sb.append(m.name).append(".rate.mean=").append(m.rateMean).append("\n");
		}

		return sb.toString();
	}


	@Override
	public String getIndex()
	{
		TemplateCall call = templater.template(PREFIX + "index.html");

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
