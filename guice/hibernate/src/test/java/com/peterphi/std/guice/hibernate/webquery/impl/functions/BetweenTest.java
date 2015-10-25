package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QProperty;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.hibernate.webquery.impl.QRelation;
import com.peterphi.std.guice.hibernate.webquery.impl.QSizeProperty;
import org.hibernate.criterion.Criterion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BetweenTest
{
	@Test
	public void testBinaryRange()
	{
		final QProperty prop = new QProperty(null, null, "x", Integer.class, false);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new Between(ref, "1","2").encode();

		assertEquals("x between 1 and 2", criterion.toString());
	}


	@Test
	public void testBinarySizeRange()
	{
		final QRelation relation = new QRelation(null, null, "children", null, false);
		final QSizeProperty prop = new QSizeProperty(relation);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new Between(ref, "1","2").encode();

		// N.B. the toString of size restrictions is incorrect due to HHH-9869 so this looks wrong
		assertEquals("children.size<=1 and children.size>=2", criterion.toString());
	}


	@Test
	public void testLeftOnlyRange()
	{
		final QProperty prop = new QProperty(null, null, "x", Integer.class, false);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new Between(ref, "1","").encode();

		assertEquals("x>=1", criterion.toString());
	}


	@Test
	public void testRightOnlyRange()
	{
		final QProperty prop = new QProperty(null, null, "x", Integer.class, false);
		final QPropertyRef ref = new QPropertyRef(null, prop);

		final Criterion criterion = new Between(ref,"", "1").encode();

		assertEquals("x<=1", criterion.toString());
	}
}
