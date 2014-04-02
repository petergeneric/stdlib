package com.peterphi.std.guice.testrestclient.server;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.metrics.StatsRegistry;
import com.peterphi.std.guice.thymeleaf.ThymeleafCall;
import com.peterphi.std.guice.thymeleaf.ThymeleafTemplater;
import com.peterphi.std.guice.web.rest.jaxrs.exception.LiteralRestResponseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.ws.rs.core.Response;
import java.util.Date;

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
		this.counter = registry.counter(MetricRegistry.name(getClass(), "failures"));
		this.thymeleafRenderTime = registry.histogram(MetricRegistry.name(getClass(), "thymeleaf.render-time"));
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


	@Override
	public String datePage(final DateTime date)
	{
		if (date == null)
		{
			return "No date specified. Please add ?date=(ISO Date) (e.g. ?date=now)";
		}

		if (isPearlHarbourDay(date))
			return "The supplied date, " + date + ", will live in infamy";
		else
			return "The supplied date, " + date + ", will not live in infamy";
	}


	/**
	 * Tests if the supplied date occurred on the day 1941-12-07 in the Hawaii timezone (UTC-10)
	 * @param date
	 * @return
	 */
	private boolean isPearlHarbourDay(DateTime date)
	{
		final LocalDate pearlHarbour = new LocalDate("1941-12-07");
		final DateTimeZone hawaii = DateTimeZone.forOffsetHours(-10);
		final Interval pearlHarbourDay = pearlHarbour.toInterval(hawaii);

		return pearlHarbourDay.contains(date);
	}
}
