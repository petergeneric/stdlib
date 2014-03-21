package com.peterphi.std.guice.metrics.rest.impl;

import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.metrics.StatsRegistry;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import org.apache.log4j.Logger;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;


@Singleton
public class MetricsRestServiceImpl implements MetricsRestService
{
	private static final Logger log = Logger.getLogger(MetricsRestServiceImpl.class);

	private final StatsRegistry stats;
	private transient ObjectMapper mapper;

	@Inject
	public MetricsRestServiceImpl(StatsRegistry stats)
	{
		this.stats = stats;
		final TimeUnit rateUnit = TimeUnit.SECONDS;
		final TimeUnit durationUnit = TimeUnit.SECONDS;
		final boolean showSamples = true;
		this.mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
		                                                                  durationUnit,
		                                                                  showSamples));
	}

	@Override
	public String get()
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
			else{
				ow =  mapper.writer();
			}

			ow.writeValue(sw, stats.getRegistry());
			return sw.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
