package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.types.MetricsCounter;
import com.peterphi.std.guice.metrics.rest.types.MetricsDocument;
import com.peterphi.std.guice.metrics.rest.types.MetricsGauge;
import com.peterphi.std.guice.metrics.rest.types.MetricsHistogram;
import com.peterphi.std.guice.metrics.rest.types.MetricsMeter;
import com.peterphi.std.guice.web.rest.templating.TemplateCall;
import com.peterphi.std.guice.web.rest.templating.thymeleaf.GuiceCoreTemplater;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Singleton
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

	private String _hostname;

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
			final Object value = entry.getValue().getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);
		}

		sb.append("# Histograms\n");

		for (Map.Entry<String, Histogram> entry : registry.getHistograms().entrySet())
		{
			final String name = toPrometheusMetricName(entry.getKey());
			final Object value = entry.getValue().getCount();

			appendMetric(sb, serviceProperties, servicePropertiesPartial, name, "counter", value);
		}

		// TODO expose timers

		return sb.toString();
	}


	private void appendMetric(final StringBuilder sb,
	                          final String label,
	                          final String labelAsSuffix,
	                          final String name,
	                          final String type,
	                          final Object value)
	{
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

		sb.append(" ").append(value).append("\n");
	}


	private String getServiceProperties()
	{
		final String hostname = getHostname();

		String serviceName = service;

		// Strip any leading slashes
		while (serviceName.startsWith("/"))
			serviceName = serviceName.substring(1);

		return "{service=\"" + serviceName + "\",host=\"" + hostname + "\"}";
	}


	private String getHostname()
	{
		if (_hostname == null)
		{
			try
			{
				_hostname = InetAddress.getLocalHost().getHostName();
			}
			catch (Throwable t)
			{
				// ignore
			}

			if (StringUtils.isEmpty(_hostname) || StringUtils.equalsIgnoreCase("localhost", _hostname))
			{
				try
				{
					final URI uri = URI.create(this.localRestEndpoint);
					_hostname = uri.getHost();
				}
				catch (Throwable t)
				{
					// ignore
					_hostname = "unknown";
				}
			}

			// Only capture the first dotted part of the hostname
			if (_hostname != null && _hostname.indexOf('.') != -1)
			{
				_hostname = StringUtils.split(_hostname, '.')[0];
			}
		}

		return _hostname;
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
