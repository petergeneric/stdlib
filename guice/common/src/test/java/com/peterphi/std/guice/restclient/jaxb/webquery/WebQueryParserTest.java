package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebQueryParserTest
{
	@Test
	public void testSimpleFragments()
	{
		assertEquals("id = 1", WebQueryParser.parse("id=1", new WebQuery()).toQueryFragment());
		assertEquals("multiline comment","id = 1", WebQueryParser.parse("/* some comment goes here */ id=1", new WebQuery()).toQueryFragment());
		assertEquals("single line comment","id = 1", WebQueryParser.parse("id=1 --some comment", new WebQuery()).toQueryFragment());
		assertEquals("single line comment","id = 1\nAND id = 2", WebQueryParser.parse("id=1 //some comment\nand id=2", new WebQuery()).toQueryFragment());
		assertEquals("single line comment in middle of expression","id = 1", WebQueryParser.parse("id= -- some comment\n1 --some other comment", new WebQuery()).toQueryFragment());

		assertEquals("name NOT STARTS alice", WebQueryParser.parse("name not starts alice", new WebQuery()).toQueryFragment());
		assertEquals("id != 1", WebQueryParser.parse("id!=1", new WebQuery()).toQueryFragment());
		assertEquals("id ~= x", WebQueryParser.parse("id~=x", new WebQuery()).toQueryFragment());
		assertEquals("id IN(1, 2, 3)", WebQueryParser.parse("id in (1,2,3)", new WebQuery()).toQueryFragment());
		assertEquals("licenses EQREF copies:size",
		             WebQueryParser.parse("licenses eqref copies:size", new WebQuery()).toQueryFragment());
		assertEquals("licenses EQREF copies:size\nAND id = 2",
		             WebQueryParser.parse("licenses eqref copies:size and id = 2", new WebQuery()).toQueryFragment());
		assertEquals("id IN(1, 2)\nAND name STARTS alice",
		             WebQueryParser.parse("id in (1,2) and name starts alice", new WebQuery()).toQueryFragment());
		assertEquals("id NOT IN(1, 2)\nAND name STARTS alice",
		             WebQueryParser.parse("id not in (1,2) and name starts alice", new WebQuery()).toQueryFragment());
		assertEquals("speed BETWEEN 1 AND 30", WebQueryParser.parse("speed between 1 and 30", new WebQuery()).toQueryFragment());
		assertEquals("dob >= now-PT5M", WebQueryParser.parse("dob >= now-PT5M", new WebQuery()).toQueryFragment());
		assertEquals("dob >= 2000-01-01T00:00:00Z",
		             WebQueryParser.parse("dob >= '2000-01-01T00:00:00Z'", new WebQuery()).toQueryFragment());
		assertEquals("Allow GT in use of >","id > 1", WebQueryParser.parse("id GT 1", new WebQuery()).toQueryFragment());

	}


	@Test
	public void testNotGroups()
	{
		assertEquals("NOT(id = 1)", WebQueryParser.parse("NOT(id=1)", new WebQuery()).toQueryFragment());
		assertEquals("NOT(id = 1 OR id = 2)", WebQueryParser.parse("NOT(id=1 OR id=2)", new WebQuery()).toQueryFragment());

		// NOTting an AND group is against the native NONE representation, so NOT(a AND b) gets inverted into OR(NOT a, NOT b)
		assertEquals("(id != 1 OR name != alice)",
		             WebQueryParser.parse("NOT(id = 1 AND name = alice)", new WebQuery()).toQueryFragment());

		assertEquals("NOT((id != 1 OR name != alice))",
		             WebQueryParser.parse("NOT(NOT(id = 1 AND name = alice))", new WebQuery()).toQueryFragment());

	}

	@Test
	public void testWithOrder()
	{
		assertEquals("id = 1 AND (name = foo OR title = dr) ORDER BY id, name DESC, title, title2",
		             WebQueryParser
				             .parse("id=1 and (name=foo or title=dr)order by id asc, name desc, title, title2", new WebQuery())
				             .toQueryFragment().replace("\t","").replace("\n", " "));
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
