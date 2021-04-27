package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebQueryParserTest
{
	@Test
	public void testSimpleFragments()
	{
		assertEquals("id = 1", WebQueryParser.parse("id=1", new WebQuery()).toQueryFragment());
		assertEquals("id != 1", WebQueryParser.parse("id!=1", new WebQuery()).toQueryFragment());
		assertEquals("id ~= x", WebQueryParser.parse("id~=x", new WebQuery()).toQueryFragment());
		assertEquals("(id = 1 OR id = 2 OR id = 3)", WebQueryParser.parse("id in (1,2,3)", new WebQuery()).toQueryFragment());
		assertEquals("licenses EQREF copies:size",
		             WebQueryParser.parse("licenses eqref copies:size", new WebQuery()).toQueryFragment());
		assertEquals("licenses EQREF copies:size AND id = 2",
		             WebQueryParser.parse("licenses eqref copies:size and id = 2", new WebQuery()).toQueryFragment());
		assertEquals("(id = 1 OR id = 2) AND name STARTS alice",
		             WebQueryParser.parse("id in (1,2) and name starts alice", new WebQuery()).toQueryFragment());
		assertEquals("speed BETWEEN 1 AND 30", WebQueryParser.parse("speed between 1 and 30", new WebQuery()).toQueryFragment());
		assertEquals("dob >= now-PT5M", WebQueryParser.parse("dob >= now-PT5M", new WebQuery()).toQueryFragment());
		assertEquals("dob >= 2000-01-01T00:00:00Z",
		             WebQueryParser.parse("dob >= '2000-01-01T00:00:00Z'", new WebQuery()).toQueryFragment());
	}


	@Test
	public void testWithOrder()
	{
		assertEquals("id = 1 AND (name = foo OR title = dr) ORDER BY id, name DESC, title, title2",
		             WebQueryParser
				             .parse("id=1 and (name=foo or title=dr)order by id asc, name desc, title, title2", new WebQuery())
				             .toQueryFragment());
	}


	@Test
	public void testEmptyQueryWithOrder()
	{
		assertEquals("empty text query", "", WebQueryParser.parse("", new WebQuery()).toQueryFragment());
		assertEquals("text query with only ordering",
		             "ORDER BY id",
		             WebQueryParser.parse("ORDER BY id", new WebQuery()).toQueryFragment());
	}
}
