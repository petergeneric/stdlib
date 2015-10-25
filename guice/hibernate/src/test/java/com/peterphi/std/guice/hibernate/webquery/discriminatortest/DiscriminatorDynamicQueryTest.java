package com.peterphi.std.guice.hibernate.webquery.discriminatortest;

import com.google.inject.Inject;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraintBuilder;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraintBuilderFactory;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties",
		            classPackages = MyBaseObject.class)
public class DiscriminatorDynamicQueryTest
{
	@Inject
	HibernateDao<MyBaseObject, Long> dao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;


	@Test
	public void testHibernateSubclassSupport()
	{
		MyChildObject1 a = new MyChildObject1();
		MyChildObject2 b = new MyChildObject2();

		a.id = dao.save(a);
		b.id = dao.save(b);

		// Make sure the right subclass is returned from a getById query
		assertTrue(dao.getById(a.id) instanceof MyChildObject1);
		assertTrue(dao.getById(b.id) instanceof MyChildObject2);
	}


	@Test
	public void testDiscriminatorInWebQuery()
	{
		MyChildObject1 a = new MyChildObject1();
		MyChildObject2 b = new MyChildObject2();

		a.id = dao.save(a);
		b.id = dao.save(b);

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();


			builder.add("id", String.valueOf(a.id), String.valueOf(b.id));
			builder.add("_class", "one");

			// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
			ConstrainedResultSet<MyBaseObject> results = dao.findByUriQuery(builder.buildQuery());

			assertEquals("discriminator should match exactly one entity", 1, results.getList().size());
			assertEquals(a.id, results.getList().get(0).id);
		}
	}
}
