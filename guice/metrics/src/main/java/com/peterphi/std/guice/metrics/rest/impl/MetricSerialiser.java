package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.health.HealthCheck;
import com.peterphi.std.guice.metrics.rest.types.HealthCheckResult;
import com.peterphi.std.guice.metrics.rest.types.HealthImplication;
import com.peterphi.std.guice.metrics.rest.types.MetricsCounter;
import com.peterphi.std.guice.metrics.rest.types.MetricsGauge;
import com.peterphi.std.guice.metrics.rest.types.MetricsHistogram;
import com.peterphi.std.guice.metrics.rest.types.MetricsMeter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

class MetricSerialiser
{
	public MetricsHistogram serialise(String name, Histogram histo)
	{
		Snapshot snapshot = histo.getSnapshot();
		final long count = histo.getCount();
		return new MetricsHistogram(name,
		                            count,
		                            snapshot.size(),
		                            snapshot.getMin(),
		                            snapshot.getMax(),
		                            snapshot.getStdDev(),
		                            snapshot.getMean(),
		                            snapshot.getMedian(),
		                            snapshot.get75thPercentile(),
		                            snapshot.get95thPercentile(),
		                            snapshot.get98thPercentile(),
		                            snapshot.get99thPercentile(),
		                            snapshot.get999thPercentile());
	}


	public MetricsGauge serialise(String name, Gauge gauge)
	{
		return new MetricsGauge(name, String.valueOf(gauge.getValue()));
	}


	public MetricsCounter serialise(String name, Counter counter)
	{
		return new MetricsCounter(name, counter.getCount());
	}


	public MetricsMeter serialise(String name, Meter meter)
	{
		return new MetricsMeter(name,
		                        meter.getCount(),
		                        meter.getOneMinuteRate(),
		                        meter.getFiveMinuteRate(),
		                        meter.getFifteenMinuteRate(),
		                        meter.getMeanRate());
	}


	public List<MetricsCounter> serialiseCounters(final Map<String, Counter> counters)
	{
		List<MetricsCounter> list = new ArrayList<>();

		for (Map.Entry<String, Counter> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public List<MetricsHistogram> serialiseHistograms(final Map<String, Histogram> counters)
	{
		List<MetricsHistogram> list = new ArrayList<>();

		for (Map.Entry<String, Histogram> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public List<MetricsGauge> serialiseGauges(final Map<String, Gauge> counters)
	{
		List<MetricsGauge> list = new ArrayList<>();

		for (Map.Entry<String, Gauge> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public List<MetricsMeter> serialiseMeters(final Map<String, Meter> counters)
	{
		List<MetricsMeter> list = new ArrayList<>();

		for (Map.Entry<String, Meter> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public List<HealthCheckResult> serialiseHealthChecks(final SortedMap<String, HealthCheck.Result> results)
	{
		List<HealthCheckResult> list = new ArrayList<>();

		for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	private HealthCheckResult serialise(String name, final HealthCheck.Result value)
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
