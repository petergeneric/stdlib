package com.mediasmiths.std.guice.metrics.rest.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.std.guice.common.metrics.StatsRegistry;
import com.mediasmiths.std.guice.metrics.rest.api.MetricsRestService;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.stats.Snapshot;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class MetricsRestServiceImpl implements MetricsRestService, MetricProcessor<MetricsRestServiceImpl.Context>
{
	private static final Logger log = Logger.getLogger(MetricsRestServiceImpl.class);

	private JsonFactory factory = new JsonFactory(new ObjectMapper());
	private final Clock clock = Clock.defaultClock();

	static final class Context
	{
		final boolean showFullSamples;
		final JsonGenerator json;


		Context(JsonGenerator json, boolean showFullSamples)
		{
			this.json = json;
			this.showFullSamples = showFullSamples;
		}
	}


	private final StatsRegistry stats;
	private final VirtualMachineMetrics vm = VirtualMachineMetrics.getInstance();


	@Inject
	public MetricsRestServiceImpl(StatsRegistry stats)
	{
		this.stats = stats;
	}


	@Override
	public String get()
	{
		try
		{
			final boolean pretty = true;
			final StringWriter sw = new StringWriter();

			final JsonGenerator json = factory.createJsonGenerator(sw);

			if (pretty)
			{
				json.useDefaultPrettyPrinter();
			}

			json.writeStartObject();
			{
				writeVmMetrics(json);

				writeRegularMetrics(json, null, false);
			}
			json.writeEndObject();
			json.close();

			return sw.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}


	private void writeVmMetrics(JsonGenerator json) throws IOException
	{
		json.writeFieldName("jvm");
		json.writeStartObject();
		{
			json.writeFieldName("vm");
			json.writeStartObject();
			{
				json.writeStringField("name", vm.name());
				json.writeStringField("version", vm.version());
			}
			json.writeEndObject();

			json.writeFieldName("memory");
			json.writeStartObject();
			{
				json.writeNumberField("totalInit", vm.totalInit());
				json.writeNumberField("totalUsed", vm.totalUsed());
				json.writeNumberField("totalMax", vm.totalMax());
				json.writeNumberField("totalCommitted", vm.totalCommitted());

				json.writeNumberField("heapInit", vm.heapInit());
				json.writeNumberField("heapUsed", vm.heapUsed());
				json.writeNumberField("heapMax", vm.heapMax());
				json.writeNumberField("heapCommitted", vm.heapCommitted());

				json.writeNumberField("heap_usage", vm.heapUsage());
				json.writeNumberField("non_heap_usage", vm.nonHeapUsage());
				json.writeFieldName("memory_pool_usages");
				json.writeStartObject();
				{
					for (Map.Entry<String, Double> pool : vm.memoryPoolUsage().entrySet())
					{
						json.writeNumberField(pool.getKey(), pool.getValue());
					}
				}
				json.writeEndObject();
			}
			json.writeEndObject();

			final Map<String, VirtualMachineMetrics.BufferPoolStats> bufferPoolStats = vm.getBufferPoolStats();
			if (!bufferPoolStats.isEmpty())
			{
				json.writeFieldName("buffers");
				json.writeStartObject();
				{
					json.writeFieldName("direct");
					json.writeStartObject();
					{
						json.writeNumberField("count", bufferPoolStats.get("direct").getCount());
						json.writeNumberField("memoryUsed", bufferPoolStats.get("direct").getMemoryUsed());
						json.writeNumberField("totalCapacity", bufferPoolStats.get("direct").getTotalCapacity());
					}
					json.writeEndObject();

					json.writeFieldName("mapped");
					json.writeStartObject();
					{
						json.writeNumberField("count", bufferPoolStats.get("mapped").getCount());
						json.writeNumberField("memoryUsed", bufferPoolStats.get("mapped").getMemoryUsed());
						json.writeNumberField("totalCapacity", bufferPoolStats.get("mapped").getTotalCapacity());
					}
					json.writeEndObject();
				}
				json.writeEndObject();
			}


			json.writeNumberField("daemon_thread_count", vm.daemonThreadCount());
			json.writeNumberField("thread_count", vm.threadCount());
			json.writeNumberField("current_time", clock.time());
			json.writeNumberField("uptime", vm.uptime());
			json.writeNumberField("fd_usage", vm.fileDescriptorUsage());

			json.writeFieldName("thread-states");
			json.writeStartObject();
			{
				for (Map.Entry<Thread.State, Double> entry : vm.threadStatePercentages().entrySet())
				{
					json.writeNumberField(entry.getKey().toString().toLowerCase(), entry.getValue());
				}
			}
			json.writeEndObject();

			json.writeFieldName("garbage-collectors");
			json.writeStartObject();
			{
				for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.garbageCollectors().entrySet())
				{
					json.writeFieldName(entry.getKey());
					json.writeStartObject();
					{
						final VirtualMachineMetrics.GarbageCollectorStats gc = entry.getValue();
						json.writeNumberField("runs", gc.getRuns());
						json.writeNumberField("time", gc.getTime(TimeUnit.MILLISECONDS));
					}
					json.writeEndObject();
				}
			}
			json.writeEndObject();
		}
		json.writeEndObject();
	}


	public void writeRegularMetrics(JsonGenerator json, String classPrefix, boolean showFullSamples) throws IOException
	{
		for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : stats.getRegistry().groupedMetrics().entrySet())
		{
			if (classPrefix == null || entry.getKey().startsWith(classPrefix))
			{
				json.writeFieldName(entry.getKey());
				json.writeStartObject();
				{
					for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet())
					{
						json.writeFieldName(subEntry.getKey().getName());
						try
						{
							subEntry.getValue().processWith(this, subEntry.getKey(), new Context(json, showFullSamples));
						}
						catch (Exception e)
						{
							log.warn("Error writing out " + subEntry.getKey(), e);
						}
					}
				}
				json.writeEndObject();
			}
		}
	}


	@Override
	public void processHistogram(MetricName name, Histogram histogram, Context context) throws Exception
	{
		final JsonGenerator json = context.json;
		json.writeStartObject();
		{
			json.writeStringField("type", "histogram");
			json.writeNumberField("count", histogram.count());
			writeSummarizable(histogram, json);
			writeSampling(histogram, json);
		}
		json.writeEndObject();
	}


	@Override
	public void processCounter(MetricName name, Counter counter, Context context) throws Exception
	{
		final JsonGenerator json = context.json;
		json.writeStartObject();
		{
			json.writeStringField("type", "counter");
			json.writeNumberField("count", counter.count());
		}
		json.writeEndObject();
	}


	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws Exception
	{
		final JsonGenerator json = context.json;
		json.writeStartObject();
		{
			json.writeStringField("type", "gauge");
			json.writeObjectField("value", evaluateGauge(gauge));
		}
		json.writeEndObject();
	}


	@Override
	public void processMeter(MetricName name, Metered meter, Context context) throws Exception
	{
		final JsonGenerator json = context.json;
		json.writeStartObject();
		{
			json.writeStringField("type", "meter");
			json.writeStringField("event_type", meter.eventType());
			writeMeteredFields(meter, json);
		}
		json.writeEndObject();
	}


	@Override
	public void processTimer(MetricName name, Timer timer, Context context) throws Exception
	{
		final JsonGenerator json = context.json;
		json.writeStartObject();
		{
			json.writeStringField("type", "timer");
			json.writeFieldName("duration");
			json.writeStartObject();
			{
				json.writeStringField("unit", timer.durationUnit().toString().toLowerCase());
				writeSummarizable(timer, json);
				writeSampling(timer, json);
				if (context.showFullSamples)
				{
					json.writeObjectField("values", timer.getSnapshot().getValues());
				}
			}
			json.writeEndObject();

			json.writeFieldName("rate");
			json.writeStartObject();
			{
				writeMeteredFields(timer, json);
			}
			json.writeEndObject();
		}
		json.writeEndObject();
	}


	private static Object evaluateGauge(Gauge<?> gauge)
	{
		try
		{
			return gauge.value();
		}
		catch (RuntimeException e)
		{
			log.warn("Error evaluating gauge", e);
			return "error reading gauge: " + e.getMessage();
		}
	}


	private static void writeSummarizable(Summarizable metric, JsonGenerator json) throws IOException
	{
		json.writeNumberField("min", metric.min());
		json.writeNumberField("max", metric.max());
		json.writeNumberField("mean", metric.mean());
		json.writeNumberField("std_dev", metric.stdDev());
	}


	private static void writeSampling(Sampling metric, JsonGenerator json) throws IOException
	{
		final Snapshot snapshot = metric.getSnapshot();
		json.writeNumberField("median", snapshot.getMedian());
		json.writeNumberField("p75", snapshot.get75thPercentile());
		json.writeNumberField("p95", snapshot.get95thPercentile());
		json.writeNumberField("p98", snapshot.get98thPercentile());
		json.writeNumberField("p99", snapshot.get99thPercentile());
		json.writeNumberField("p999", snapshot.get999thPercentile());
	}


	private static void writeMeteredFields(Metered metered, JsonGenerator json) throws IOException
	{
		json.writeStringField("unit", metered.rateUnit().toString().toLowerCase());
		json.writeNumberField("count", metered.count());
		json.writeNumberField("mean", metered.meanRate());
		json.writeNumberField("m1", metered.oneMinuteRate());
		json.writeNumberField("m5", metered.fiveMinuteRate());
		json.writeNumberField("m15", metered.fifteenMinuteRate());
	}
}
