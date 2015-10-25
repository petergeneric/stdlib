package com.peterphi.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
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
		            classPackages = ParentEntity.class)
public class DynamicQueryTest
{
	@Inject
	HibernateDao<ParentEntity, Long> dao;

	@Inject
	HibernateDao<ChildEntity, Long> childDao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;

	@Inject
	QEntityFactory factory;


	@Transactional
	@Before
	public void clearDatabaseBeforeTest()
	{
		for (ParentEntity obj : dao.getAll())
			dao.delete(obj);

		assertEquals(0, dao.getAll().size());
	}


	@Test
	public void testNestedAssociatorConstraintWorks() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.parent.name", "Alice");

		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(builder.buildQuery());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNestedAssociatorThatIsMadeUpDoesNotWork() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();


		builder.add("otherObject.parent.fictionalfield.name", "Alice");

		// Nonsense field shouldn't work
		dao.findByUriQuery(builder.buildQuery());
	}


	@Test
	public void testGetByUniqueProperty() throws Exception
	{
		// Hibernate will throw if the property doesn't work
		dao.getByUniqueProperty("otherObject.id", 1L);
	}


	@Test
	public void testGetByUniquePropertyOnCollection() throws Exception
	{
		// Hibernate will throw if the property doesn't work
		dao.getByUniqueProperty("children.id", 1L);
	}


	@Test
	public void testNestedAssociatorConstraintWorksInGetByUniqueProperty() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		// Hibernate will throw if the join doesn't get automatically set up
		dao.getByUniqueProperty("otherObject.parent.name", "Alice");
	}


	@Test
	public void testGetByRelationIdIsNull() throws Exception
	{
		ParentEntity obj = new ParentEntity();
		obj.setName("Name");
		dao.save(obj);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.id", "_null");

		assertEquals(1, dao.findByUriQuery(builder.buildQuery()).getList().size());
	}


	/**
	 * Test that the dynamic query logic sees a ManyToOne which references a column in another Entity (a property of type
	 * CollectionType)
	 *
	 * @throws IllegalArgumentException
	 * 		if the property is not correctly parsed and available to queries
	 */
	@Test
	public void testDynamicQuerySeesManyToOneRelation() throws IllegalArgumentException
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("children.id", "_null");

		// Will throw an IllegalArgumentException if the "children" field is not parsed from the entity
		dao.findByUriQuery(builder.buildQuery());
	}


	@Test
	public void testByBooleanField() throws Exception
	{
		{
			ParentEntity obj1 = new ParentEntity();
			obj1.setName("Name1");
			obj1.setDeprecated(true);
			dao.save(obj1);

			ParentEntity obj2 = new ParentEntity();
			obj2.setName("Name2");
			obj2.setDeprecated(true);
			dao.save(obj2);
		}

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "true");

			assertEquals("deprecated=true matches 2", 2, dao.findByUriQuery(builder.buildQuery()).getList().size());
		}

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "false");

			assertEquals("deprecated=false matches nothing", 0, dao.findByUriQuery(builder.buildQuery()).getList().size());
		}
	}


	@Test
	public void testOrderAsc() throws Exception
	{
		ParentEntity obj1 = new ParentEntity();
		obj1.setName("Name1");
		dao.save(obj1);

		ParentEntity obj2 = new ParentEntity();
		obj2.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.addOrder("id asc");

		assertEquals(getIds(Arrays.asList(obj1, obj2)), getIds(dao.findByUriQuery(builder.buildQuery()).getList()));
	}


	@Test
	public void testOrderDesc() throws Exception
	{
		ParentEntity obj1 = new ParentEntity();
		obj1.setName("Name1");
		dao.save(obj1);

		ParentEntity obj2 = new ParentEntity();
		obj2.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("_order", "id desc");

		assertEquals(getIds(obj2, obj1), getIds(dao.findByUriQuery(builder.buildQuery()).getList()));
	}


	@Test
	public void testComputeSize() throws Exception
	{
		{
			ParentEntity obj1 = new ParentEntity();
			obj1.setName("Name1");
			dao.save(obj1);

			ParentEntity obj2 = new ParentEntity();
			obj2.setName("Name2");
			dao.save(obj2);
		}

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("_limit", "1");
		builder.add("_compute_size", "true");

		ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(builder.buildQuery());

		assertEquals(1, results.getList().size());
		assertEquals(Long.valueOf(2), results.getTotal());
	}


	/**
	 * Test that it is possible to search based on the size of a collection
	 *
	 * @throws Exception
	 * 		on error
	 */
	@Test
	public void testConstrainSize() throws Exception
	{
		// Set up 2 MyObject instances, one of which has 2 child instances
		{
			ParentEntity obj1 = new ParentEntity();
			obj1.setName("Name1");
			dao.save(obj1);

			ParentEntity obj2 = new ParentEntity();
			obj2.setName("Name2");
			dao.save(obj2);

			ChildEntity child1a = new ChildEntity();
			child1a.setParent(obj1);
			child1a.setName("a");
			childDao.save(child1a);

			ChildEntity child1b = new ChildEntity();
			child1b.setParent(obj1);
			child1b.setName("b");
			childDao.save(child1b);
		}

		// Search for entries without constraints
		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(builder.buildQuery());

			assertEquals(2, results.getList().size());
		}

		// Search for entries with at least 2 children
		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("children:size", "_f_ge_2");

			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(builder.buildQuery());

			assertEquals(1, results.getList().size());
			assertEquals("Name1", results.getList().get(0).getName());
		}

		// Search for entries with no children
		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("children:size", "0");

			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(builder.buildQuery());

			assertEquals(1, results.getList().size());
			assertEquals("Name2", results.getList().get(0).getName());
		}
	}


	private List<Long> getIds(ParentEntity... objs)
	{
		return getIds(Arrays.asList(objs));
	}


	private List<Long> getIds(List<ParentEntity> objs)
	{
		List<Long> list = new ArrayList<>();
		for (ParentEntity obj : objs)
			list.add(obj.getId());

		return list;
	}
}
