package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Snapshot;
import com.peterphi.std.guice.metrics.rest.types.MetricsCounter;
import com.peterphi.std.guice.metrics.rest.types.MetricsGauge;
import com.peterphi.std.guice.metrics.rest.types.MetricsHistogram;
import com.peterphi.std.guice.metrics.rest.types.MetricsMeter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MetricSerialiser
{
	public static MetricsHistogram serialise(String name, Histogram histo)
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


	public static MetricsGauge serialise(String name, Gauge gauge)
	{
		return new MetricsGauge(name, String.valueOf(gauge.getValue()));
	}


	public static MetricsCounter serialise(String name, Counter counter)
	{
		return new MetricsCounter(name, counter.getCount());
	}


	public static MetricsMeter serialise(String name, Meter meter)
	{
		return new MetricsMeter(name,
		                        meter.getCount(),
		                        meter.getOneMinuteRate(),
		                        meter.getFiveMinuteRate(),
		                        meter.getFifteenMinuteRate(),
		                        meter.getMeanRate());
	}


	public static List<MetricsCounter> serialiseCounters(final Map<String, Counter> counters)
	{
		List<MetricsCounter> list = new ArrayList<>();

		for (Map.Entry<String, Counter> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public static List<MetricsHistogram> serialiseHistograms(final Map<String, Histogram> counters)
	{
		List<MetricsHistogram> list = new ArrayList<>();

		for (Map.Entry<String, Histogram> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public static List<MetricsGauge> serialiseGauges(final Map<String, Gauge> counters)
	{
		List<MetricsGauge> list = new ArrayList<>();

		for (Map.Entry<String, Gauge> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}


	public static List<MetricsMeter> serialiseMeters(final Map<String, Meter> counters)
	{
		List<MetricsMeter> list = new ArrayList<>();

		for (Map.Entry<String, Meter> entry : counters.entrySet())
			list.add(serialise(entry.getKey(), entry.getValue()));

		return list;
	}
}
