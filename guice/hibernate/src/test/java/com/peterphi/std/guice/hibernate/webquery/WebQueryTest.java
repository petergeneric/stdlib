package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class WebQueryTest
{
	@Test
	public void testEncodeResultStable()
	{
		WebQuery query = new WebQuery()
				                 .orderDesc("id")
				                 .orderAsc("name")
				                 .orderAsc("name2")
				                 .limit(99)
				                 .computeSize(true)
				                 .or(g -> g.eq("id", 1).contains("id", "some_value"))
				                 .eq("name", "x");

		assertEquals(
				"{_compute_size=[true], _fetch=[entity], _order=[id desc, name asc, name2 asc], _limit=[99], name=[x], id=[1, _f_contains_some_value]}",
				query.encode().toString());
	}


	@Test
	public void testParseQueryStringToWebQuery()
	{
		WebQuery expected = new WebQuery().logSQL(true).eq("id", "123").offset(0).limit(200);
		WebQuery actual = new WebQuery().decode(new ResteasyUriInfo(URI.create("http://example.com?_log_sql=true&id=123")));

		assertEquals(expected.encode().toString(), actual.encode().toString());
	}
}
