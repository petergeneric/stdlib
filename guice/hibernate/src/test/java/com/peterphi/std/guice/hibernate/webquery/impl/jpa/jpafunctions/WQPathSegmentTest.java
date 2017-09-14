package com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WQPathSegmentTest
{
	@Test
	public void testSegmentWithAlias()
	{
		WQPathSegment seg = new WQPathSegment("property[alias]");

		assertEquals("property", seg.getPath());
		assertEquals("[alias]", seg.getAlias());
	}
	@Test
	public void testSegmentWithoutAlias()
	{
		WQPathSegment seg = new WQPathSegment("property");

		assertEquals("property", seg.getPath());
		assertEquals("[property]", seg.getAlias());
	}
}
