package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QProperty;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RangeFunctionTest
{
	@Test
	public void testBinaryRange()
	{
		final QProperty prop = new QProperty(null, "x", Integer.class);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new RangeFunction(ref, "1..2").encode();

		assertEquals("x between 1 and 2", criterion.toString());
	}


	@Test
	public void testLeftOnlyRange()
	{
		final QProperty prop = new QProperty(null, "x", Integer.class);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new RangeFunction(ref, "1..").encode();

		assertEquals("x>=1", criterion.toString());
	}


	@Test
	public void testRightOnlyRange()
	{
		final QProperty prop = new QProperty(null, "x", Integer.class);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new RangeFunction(ref, "..1").encode();

		assertEquals("x<=1", criterion.toString());
	}
}
