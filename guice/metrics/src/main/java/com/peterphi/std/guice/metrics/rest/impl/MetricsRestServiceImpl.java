package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.auth.annotations.AuthConstraint;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.types.MetricsDocument;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

@Singleton
@AuthConstraint(id = "metrics", role = "framework-admin")
public class MetricsRestServiceImpl implements MetricsRestService
{
	private static final String PREFIX = "/com/peterphi/std/guice/metrics/rest/impl/";

	@Inject
	MetricRegistry registry;

	@Inject
	GuiceCoreTemplater templater;

	@Inject(optional = true)
	@Named(GuiceProperties.SERVLET_CONTEXT_NAME)
	public String service;

	@Inject(optional = true)
	@Named(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT)
	public String localRestEndpoint;

	@Inject(optional = true)
	@Named(GuiceProperties.METRIC_CUSTOM_LABELS)
	public String customLabels;

	private String _metricLabels;

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
	public String getPrometheusMetrics()
	{
		final String serviceProperties = getServiceProperties();
		final String servicePropertiesPartial = "," + getServiceProperties().substring(1);

		StringBuilder sb = new StringBuilder(16 * 1024);

		sb.append("# Counters\n");

		for (Map.Entry<String, Counter> entry : registry.getCounters().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final long value = entry.getValue().getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);
		}

		sb.append("# Gauges\n");

		for (Map.Entry<String, Gauge> entry : registry.getGauges().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final Object value = entry.getValue().getValue();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "gauge", value);
		}

		sb.append("# Meters\n");

		for (Map.Entry<String, Meter> entry : registry.getMeters().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final Object value = entry.getValue().getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);
		}

		sb.append("# Timers\n");

		for (Map.Entry<String, Timer> entry : registry.getTimers().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final Timer timer = entry.getValue();
			final Object value = timer.getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);

			final Snapshot snap = timer.getSnapshot();
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p75_ns"), "gauge",
			             snap.get75thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p95_ns"), "gauge",
			             snap.get95thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p99_ns"), "gauge",
			             snap.get99thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p999_ns"), "gauge",
			             snap.get999thPercentile());
		}

		sb.append("# Histograms\n");

		for (Map.Entry<String, Histogram> entry : registry.getHistograms().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final Histogram histo = entry.getValue();
			final Object value = histo.getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);

			final Snapshot snap = histo.getSnapshot();
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p75_ns"), "gauge",
			             snap.get75thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p95_ns"), "gauge",
			             snap.get95thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p99_ns"), "gauge",
			             snap.get99thPercentile());
			appendMetric(sb, serviceProperties, servicePropertiesPartial, toPrometheusMetricName(entry.getKey() + "_p999_ns"), "gauge",
			             snap.get999thPercentile());
		}

		return sb.toString();
	}


	private void appendMetric(final StringBuilder sb,
	                          final String label,
	                          final String labelAsSuffix,
	                          final String name,
	                          final String type,
	                          final Object value)
	{
		final String str = Objects.toString(value);

		// primarily designed to catch "jvm.thread-states.deadlocks=[]"
		if (str.indexOf('[') != -1)
			return; // Ignore this metric

		sb.append("# TYPE ");

		final boolean nameHasLabel = name.endsWith("}");
		if (!nameHasLabel)
			sb.append(name);
		else
			sb.append(StringUtils.split(name, '{')[0]);

		sb.append(" ").append(type).append("\n");


		if (nameHasLabel)
			sb.append(StringUtils.removeEnd(name, "}")).append(labelAsSuffix);
		else
			sb.append(name).append(label);

		sb.append(" ").append(str).append("\n");
	}


	/**
	 * Gets (lazy-generating) service properties
	 *
	 * @return
	 */
	private String getServiceProperties()
	{
		if (_metricLabels == null)
		{
			final String hostname = getHostname();

			String serviceName = service;

			// Strip any leading slashes
			while (serviceName.startsWith("/"))
				serviceName = serviceName.substring(1);

			StringBuilder sb = new StringBuilder();

			sb.append("{service=\"").append(serviceName).append("\",host=\"").append(hostname).append('"');

			if (StringUtils.isNotEmpty(customLabels))
			{
				sb.append(',');
				sb.append(customLabels);
			}

			sb.append('}');

			_metricLabels = sb.toString();
		}

		return _metricLabels;
	}


	private String getHostname()
	{
		String hostname = null;
		try
		{
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch (Throwable t)
		{
			// ignore
		}

		if (StringUtils.isEmpty(hostname) || StringUtils.equalsIgnoreCase("localhost", hostname))
		{
			try
			{
				final URI uri = URI.create(this.localRestEndpoint);
				hostname = uri.getHost();
			}
			catch (Throwable t)
			{
				// ignore
				hostname = "unknown";
			}
		}

		// Only capture the first dotted part of the hostname
		if (hostname != null && hostname.indexOf('.') != -1)
		{
			hostname = StringUtils.split(hostname, '.')[0];
		}


		return hostname;
	}


	private String toPrometheusMetricName(final String name)
	{
		if (name.indexOf('{') == -1)
		{
			return name.replace('.', '_').replace('-', '_');
		}
		else
		{
			final String[] parts = StringUtils.split(name, '{');
			return parts[0].replace('.', '_').replace('-', '_') + "{" + parts[1];
		}
	}


	@Override
	public String getIndex()
	{
		TemplateCall call = templater.template(PREFIX + "index.html");

		call.set("gauges", registry.getGauges().entrySet());
		call.set("counters",
		         this.<Counting>combine(registry.getCounters(),
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
