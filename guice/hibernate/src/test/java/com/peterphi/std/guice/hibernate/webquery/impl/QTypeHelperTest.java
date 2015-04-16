package com.peterphi.std.guice.hibernate.webquery.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class QTypeHelperTest
{
	@Test
	public void testParseSimpleDateTime()
	{

		assertEquals(new DateTime("2013-01-01T09:00:00Z"), QTypeHelper.parse(DateTime.class, "2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z"), QTypeHelper.parse(DateTime.class, "2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z"), QTypeHelper.parse(DateTime.class, "2013-01-01TZ"));
	}


	/**
	 * Test correst parsing of "today", "tomorrow" and "yesterday"
	 */
	@Test
	public void testParseFancyDateTime()
	{
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay(), QTypeHelper.parse(DateTime.class, "today"));
		assertEquals(LocalDate.now().plusDays(1).toDateTimeAtStartOfDay(), QTypeHelper.parse(DateTime.class, "tomorrow"));
		assertEquals(LocalDate.now().minusDays(1).toDateTimeAtStartOfDay(), QTypeHelper.parse(DateTime.class, "yesterday"));
	}

	@Test
	public void testDateMaths() {
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().plusMinutes(5), QTypeHelper.parse(DateTime.class, "today+PT5M"));
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().plusMinutes(5), QTypeHelper.parse(DateTime.class, "today PT5M"));
		assertEquals(LocalDate.now().toDateTimeAtStartOfDay().minusMinutes(5), QTypeHelper.parse(DateTime.class, "today-PT5M"));
	}


	@Test
	public void testParseSimpleDate()
	{
		assertEquals(new DateTime("2013-01-01T09:00:00Z").toDate(), QTypeHelper.parse(Date.class, "2013-01-01T09:00:00Z"));
		assertEquals(new DateTime("2013-01-01T09:00:00.10Z").toDate(), QTypeHelper.parse(Date.class, "2013-01-01T09:00:00.10Z"));
		assertEquals(new DateTime("2013-01-01T00:00:00Z").toDate(), QTypeHelper.parse(Date.class, "2013-01-01TZ"));
	}


	@Test
	public void testParseBoolean()
	{
		assertEquals(true, QTypeHelper.parse(Boolean.class, "true"));
		assertEquals(true, QTypeHelper.parse(Boolean.class, "yes"));
		assertEquals(true, QTypeHelper.parse(Boolean.class, "on"));

		assertEquals(false, QTypeHelper.parse(Boolean.class, "false"));
		assertEquals(false, QTypeHelper.parse(Boolean.class, "no"));
		assertEquals(false, QTypeHelper.parse(Boolean.class, "off"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testParseBooleanNonsenseFails() throws Exception
	{
		QTypeHelper.parse(Boolean.class, "ajfaskjdfhakjshasjd");
	}
}
