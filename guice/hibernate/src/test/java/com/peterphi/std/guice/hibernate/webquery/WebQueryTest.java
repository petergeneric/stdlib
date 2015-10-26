package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.junit.Test;

public class WebQueryTest
{
	@Test
	public void testEncode()
	{
		new WebQuery().orderDesc("id")
		              .orderAsc("name")
		              .orderAsc("name2")
		              .limit(99)
		              .computeSize(true)
		              .or(g -> g.eq("id", 1)
		                        .contains("id",
		                                  "some_value"))
		              .eq("name", "x")
		              .encode();
	}
}
