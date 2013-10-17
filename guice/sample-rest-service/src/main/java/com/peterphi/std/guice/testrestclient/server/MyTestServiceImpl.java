package com.peterphi.std.guice.testrestclient.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.metrics.StatsRegistry;
import com.peterphi.std.guice.thymeleaf.ThymeleafCall;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.jaxrs.exception.LiteralRestResponseException;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

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
		final MetricsRegistry registry = stats.getRegistry();

		this.meter = registry.newMeter(getClass(), "index-page", "calls", TimeUnit.SECONDS); // calls per second
		this.counter = registry.newCounter(getClass(), "failures"); // failures
		this.thymeleafRenderTime = registry.newHistogram(getClass(), "thymeleaf.render-time");
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
