package com.peterphi.std.guice.hibernate.webquery.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class WQTypeHelperTest
{
	@Test
	public void testParseSimpleDateTime()
	{

		assertEquals(new DateTime("2013-01-01T09:00:00Z"), WQTypeHelper.parse(DateTime.class, "2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z"), WQTypeHelper.parse(DateTime.class, "2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z"), WQTypeHelper.parse(DateTime.class, "2013-01-01TZ"));
	}


	/**
	 * Test correst parsing of "today", "tomorrow" and "yesterday"
	 */
	@Test
	public void testParseFancyDateTime()
	{
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay(), WQTypeHelper.parse(DateTime.class, "today"));
		assertEquals(LocalDate.now().plusDays(1).toDateTimeAtStartOfDay(), WQTypeHelper.parse(DateTime.class, "tomorrow"));
		assertEquals(LocalDate.now().minusDays(1).toDateTimeAtStartOfDay(), WQTypeHelper.parse(DateTime.class, "yesterday"));
	}

	@Test
	public void testDateMaths() {
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().plusMinutes(5), WQTypeHelper.parse(DateTime.class, "today+PT5M"));
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().plusMinutes(5), WQTypeHelper.parse(DateTime.class, "today PT5M"));
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().minusMinutes(5), WQTypeHelper.parse(DateTime.class, "today-PT5M"));
	}


	@Test
	public void testParseSimpleDate()
	{
		assertEquals(new DateTime("2013-01-01T09:00:00Z").toDate(), WQTypeHelper.parse(Date.class, "2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z").toDate(), WQTypeHelper.parse(Date.class, "2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z").toDate(), WQTypeHelper.parse(Date.class, "2013-01-01TZ"));
	}


	@Test
	public void testParseBoolean()
	{
		assertEquals(true, WQTypeHelper.parse(Boolean.class, "true"));
		assertEquals(true, WQTypeHelper.parse(Boolean.class, "yes"));
		assertEquals(true, WQTypeHelper.parse(Boolean.class, "on"));

		assertEquals(false, WQTypeHelper.parse(Boolean.class, "false"));
		assertEquals(false, WQTypeHelper.parse(Boolean.class, "no"));
		assertEquals(false, WQTypeHelper.parse(Boolean.class, "off"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testParseBooleanNonsenseFails() throws Exception
	{
		WQTypeHelper.parse(Boolean.class, "ajfaskjdfhakjshasjd");
	}
}
