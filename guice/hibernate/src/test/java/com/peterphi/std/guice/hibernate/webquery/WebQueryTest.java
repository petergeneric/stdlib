package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryParser;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WebQueryTest
{
	@Test
	public void testEncodeResultStable()
	{
		HibernateDao dao;

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


	@Test
	public void testParseComplexQueryStringToWebQuery()
	{
		WebQuery actual = new WebQuery().decode(new ResteasyUriInfo(URI.create("http://example.com/search?q=id+%3C+100+AND+id+%3E+200+AND+%28id+%3D+1+OR+id+%3D+2+OR+%28id+%3D+3+AND+id+%3D+4%29%29")));

		assertEquals("id < 100\nAND id > 200\nAND (id = 1 OR id = 2 OR (id = 3 AND id = 4))", actual.constraints.toQueryFragment());
	}


	/**
	 * Tests that a complex query that cannot easily be represented using the expanded Query String form is converted to a text
	 * query using q=...
	 */
	@Test
	public void testEncodeOfComplexQuery()
	{
		final UriBuilder ub = UriBuilder.fromUri(URI.create("/search"));

		for (Map.Entry<String, List<String>> kvp : WebQueryParser
				                                           .parse("id < 100 and id > 200 and (id=1 or id=2 or (id=3 and id=4))", new WebQuery())
				                                           .encode()
				                                           .entrySet())
		{
			if (!kvp.getKey().startsWith("_"))
			{
				String[] array = new String[kvp.getValue().size()];
				kvp.getValue().toArray(array);
				ub.queryParam(kvp.getKey(), array);
			}
		}

		assertEquals("/search?q=id+%3C+100%0AAND+id+%3E+200%0AAND+%28id+%3D+1+OR+id+%3D+2+OR+%28id+%3D+3+AND+id+%3D+4%29%29", ub.build().toASCIIString());
	}
}
