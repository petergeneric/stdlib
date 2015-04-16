package com.peterphi.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties",
		            classPackages = MyObject.class)
public class DynamicQueryTest
{
	@Inject
	HibernateDao<MyObject, Long> dao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;


	@Transactional
	@Before
	public void clearDatabaseBeforeTest()
	{
		for (MyObject obj : dao.getAll())
			dao.delete(obj);

		assertEquals(0, dao.getAll().size());
	}


	@Test
	public void testNestedAssociatorConstraintWorks() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.parent.name", "Alice");

		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(builder.build());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNestedAssociatorThatIsMadeUpDoesNotWork() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();


		builder.add("otherObject.parent.fictionalfield.name", "Alice");

		// Nonsense field shouldn't work
		dao.findByUriQuery(builder.build());
	}


	@Test
	public void testGetByRelationIdIsNull() throws Exception
	{
		MyObject obj = new MyObject();
		obj.setName("Name");
		dao.save(obj);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.id", "_null");

		assertEquals(1, dao.findByUriQuery(builder.build()).getList().size());
	}


	@Test
	public void testByBooleanField() throws Exception
	{
		{
			MyObject obj1 = new MyObject();
			obj1.setName("Name1");
			obj1.setDeprecated(true);
			dao.save(obj1);

			MyObject obj2 = new MyObject();
			obj2.setName("Name2");
			obj2.setDeprecated(true);
			dao.save(obj2);
		}

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "true");

			assertEquals("deprecated=true matches 2", 2, dao.findByUriQuery(builder.build()).getList().size());
		}

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "false");

			assertEquals("deprecated=false matches nothing", 0, dao.findByUriQuery(builder.build()).getList().size());
		}
	}


	@Test
	public void testOrderAsc() throws Exception
	{
		MyObject obj1 = new MyObject();
		obj1.setName("Name1");
		dao.save(obj1);

		MyObject obj2 = new MyObject();
		obj2.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.addOrder("id asc");

		assertEquals(getIds(Arrays.asList(obj1, obj2)), getIds(dao.findByUriQuery(builder.build()).getList()));
	}


	@Test
	public void testOrderDesc() throws Exception
	{
		MyObject obj1 = new MyObject();
		obj1.setName("Name1");
		dao.save(obj1);

		MyObject obj2 = new MyObject();
		obj2.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("_order", "id desc");

		assertEquals(getIds(obj2, obj1), getIds(dao.findByUriQuery(builder.build()).getList()));
	}

	@Test
	public void testComputeSize() throws Exception
	{
		{
			MyObject obj1 = new MyObject();
			obj1.setName("Name1");
			dao.save(obj1);

			MyObject obj2 = new MyObject();
			obj2.setName("Name2");
			dao.save(obj2);
		}

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("_limit", "1");
		builder.add("_compute_size", "true");

		ConstrainedResultSet<MyObject> results = dao.findByUriQuery(builder.build());

		assertEquals(1, results.getList().size());
		assertEquals(Long.valueOf(2), results.getTotal());
	}


	private List<Long> getIds(MyObject... objs)
	{
		return getIds(Arrays.asList(objs));
	}


	private List<Long> getIds(List<MyObject> objs)
	{
		List<Long> list = new ArrayList<>();
		for (MyObject obj : objs)
			list.add(obj.getId());

		return list;
	}
}
