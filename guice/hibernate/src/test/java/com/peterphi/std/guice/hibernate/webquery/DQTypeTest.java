package com.peterphi.std.guice.hibernate.webquery;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DQTypeTest
{
	@Test
	public void testParseSimpleDateTime()
	{
		assertEquals(new DateTime("2013-01-01T09:00:00Z"), new DQType(DateTime.class).parse("2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z"), new DQType(DateTime.class).parse("2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z"), new DQType(DateTime.class).parse("2013-01-01TZ"));
	}


	@Test
	public void testParseFancyeDateTime()
	{
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay(), new DQType(DateTime.class).parse("today"));
		assertEquals(LocalDate.now().plusDays(1).toDateTimeAtStartOfDay(), new DQType(DateTime.class).parse("tomorrow"));
		assertEquals(LocalDate.now().minusDays(1).toDateTimeAtStartOfDay(), new DQType(DateTime.class).parse("yesterday"));
	}


	@Test
	public void testParseSimpleDate()
	{
		assertEquals(new DateTime("2013-01-01T09:00:00Z").toDate(), new DQType(Date.class).parse("2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z").toDate(), new DQType(Date.class).parse("2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z").toDate(), new DQType(Date.class).parse("2013-01-01TZ"));
	}


	@Test
	public void testParseBoolean()
	{
		assertEquals(true, new DQType(Boolean.class).parse("true"));
		assertEquals(true, new DQType(Boolean.class).parse("yes"));
		assertEquals(false, new DQType(Boolean.class).parse("false"));
		assertEquals(false, new DQType(Boolean.class).parse("no"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testParseBooleanNonsenseFails() throws Exception
	{
		new DQType(Boolean.class).parse("ajfaskjdfhakjshasjd");
	}
}
