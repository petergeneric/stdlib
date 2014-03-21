package com.peterphi.std.guice.testrestclient.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.metrics.StatsRegistry;
import com.peterphi.std.guice.thymeleaf.ThymeleafCall;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.jaxrs.exception.LiteralRestResponseException;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Singleton
public class MyTestServiceImpl implements MyTestService
{
	@Inject
	public ThymeleafTemplater templater;

	private final Counter counter;
	private final Histogram thymeleafRenderTime;
	private final Meter meter;


	@Inject
	public MyTestServiceImpl(StatsRegistry stats)
	{
		final MetricRegistry registry = stats.getRegistry();

		this.meter = registry.meter(MetricRegistry.name(getClass(), "index-page", "calls"));
		this.counter = registry.counter(MetricRegistry.name(getClass(),"failures"));
		this.thymeleafRenderTime = registry.histogram(MetricRegistry.name(getClass(),"thymeleaf.render-time"));
	}


	@Override
	public String index()
	{
		meter.mark();
		return "This is an index page";
	}


	@Override
	public String fail()
	{
		counter.inc();
		throw new IllegalArgumentException("Illegal Argument!", new RuntimeException("Some cause"));
	}


	@Override
	public String fail2()
	{
		counter.inc();
		throw new LiteralRestResponseException(Response.ok("This is a literal response").build());
	}


	@Override
	public String indexPage()
	{
		meter.mark();

		final long startTime = System.nanoTime();
		try
		{
			ThymeleafCall template = templater.template("index");
			template.set("theTime", new Date());

			return template.process();
		}
		finally
		{
			thymeleafRenderTime.update(System.nanoTime() - startTime);
		}
	}
}
