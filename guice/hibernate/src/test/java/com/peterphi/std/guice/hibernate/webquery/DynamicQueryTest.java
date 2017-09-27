package com.peterphi.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.JPASearchStrategy;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties", classPackages = ParentEntity.class)
public class DynamicQueryTest
{
	@Inject
	HibernateDao<ParentEntity, Long> dao;

	@Inject
	HibernateDao<ChildEntity, Long> childDao;

	@Inject
	HibernateDao<MappedSuperclassEntity, Long> mappedSuperclassEntityDao;

	@Inject
	QEntityFactory entityFactory;


	@Transactional
	@Before
	public void clearDatabaseBeforeTest()
	{
		for (ChildEntity obj : childDao.getAll())
			childDao.delete(obj);

		for (ParentEntity obj : dao.getAll())
			dao.delete(obj);

		assertEquals(0, dao.getAll().size());
	}


	@Test
	public void testMappedSuperclassFieldSearch()
	{
		mappedSuperclassEntityDao.findByUriQuery(new WebQuery().eq("id", 123).neq("name", "test").lt("created", DateTime.now()));
	}


	@Test
	public void testCreateSchema()
	{
		final Set<String> names = entityFactory.encode().entities.stream().map(s -> s.name).collect(Collectors.toSet());

		assertTrue(names.contains("inherit_base"));
		assertTrue(names.contains("inherit_one"));
		assertTrue(names.contains("inherit_two"));
	}


	@Test
	public void testNestedAssociatorConstraintWorks() throws Exception
	{
		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(new WebQuery().eq("otherObject.parent.name", "Alice"));
	}


	/**
	 * This does not work natively with HSQLDB because HSQLDB cannot perform an ORDER BY on a column that isn't SELECTed, so this
	 * test confirms that WebQuery is able to implement it
	 *
	 * @throws Exception
	 */
	@Test
	public void testOrderingByLazyAssociatedRelationThatIsNotSelectedWorks() throws Exception
	{
		dao.findByUriQuery(new WebQuery().orderAsc("otherObject.name"));
	}


	@Test
	public void testPropertyRefWorks() throws Exception
	{
		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(new WebQuery().eqRef("otherObject.parent.name", "otherObject.parent.name"));

		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(new WebQuery().leRef("otherObject.parent.name", "otherObject.parent.name"));
	}


	@Test
	public void testEntityWrappedId() throws Exception
	{
		ParentEntity obj = new ParentEntity();
		obj.setName("Name");
		obj.setOtherObject(new ChildEntity());
		obj.getOtherObject().setName("Name");

		childDao.save(obj.getOtherObject());
		dao.save(obj);

		final ConstrainedResultSet<ParentEntity> results = dao.find(new WebQuery(), JPASearchStrategy.ENTITY_WRAPPED_ID);

		assertEquals(1, results.list.size());
		assertNotNull(results.uniqueResult().getId()); // should be populated
		assertNull(results.uniqueResult().getName()); // should not be populated
	}


	@Test
	public void testEqRefReturnsValue() throws Exception
	{
		ParentEntity obj = new ParentEntity();
		obj.setName("Name");
		obj.setOtherObject(new ChildEntity());
		obj.getOtherObject().setName("Name");

		childDao.save(obj.getOtherObject());
		dao.save(obj);

		assertEquals(1, dao.findByUriQuery(new WebQuery().eqRef("name", "otherObject.name")).getList().size());
		assertEquals(0, dao.findByUriQuery(new WebQuery().neqRef("name", "otherObject.name")).getList().size());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNestedAssociatorThatIsMadeUpDoesNotWork() throws Exception
	{
		WebQuery query = new WebQuery().eq("otherObject.parent.fictionalfield.name", "Alice");

		// Nonsense field shouldn't work
		dao.findByUriQuery(query);
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
	public void testGetByUniquePropertyWithAliasOnCollection() throws Exception
	{
		dao.getByUniqueProperty("children[r0].id", 1L);
	}


	@Test
	public void testGetUsingPropertiesOfEmbeddedCollection() throws Exception
	{
		// Hibernate will throw if the property doesn't work
		WebQuery q = new WebQuery();
		q.eq("friends[r0].firstName", "Firstname");
		q.eq("friends[r0].lastName", "Surname");

		dao.findByUriQuery(q);
	}


	@Test
	public void testNestedAssociatorConstraintWorksInGetByUniqueProperty() throws Exception
	{
		// Hibernate will throw if the join doesn't get automatically set up
		dao.getByUniqueProperty("otherObject.parent.name", "Alice");
	}


	@Test
	public void testGetByRelationIdIsNull() throws Exception
	{
		ParentEntity obj = new ParentEntity();
		obj.setName("Name");
		dao.save(obj);

		assertEquals(1, dao.findByUriQuery(new WebQuery().isNull("otherObject.id")).getList().size());
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
		// Will throw an IllegalArgumentException if the "children" field is not parsed from the entity
		dao.findByUriQuery(new WebQuery().isNull("children.id"));
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

		assertEquals("deprecated=true matches 2 rows",
		             2,
		             dao.findByUriQuery(new WebQuery().eq("deprecated", true)).getList().size());

		assertEquals("deprecated=false matches nothing",
		             0,
		             dao.findByUriQuery(new WebQuery().eq("deprecated", false)).getList().size());
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

		assertEquals(getIds(Arrays.asList(obj1, obj2)), getIds(dao.findByUriQuery(new WebQuery().orderAsc("id")).getList()));
	}


	@Test
	public void testGetIdList() throws Exception
	{
		ParentEntity obj1 = new ParentEntity();
		obj1.setName("Name1");
		dao.save(obj1);

		ParentEntity obj2 = new ParentEntity();
		obj2.setName("Name2");
		dao.save(obj2);

		assertEquals(getIds(Arrays.asList(obj1, obj2)), dao.getIdList(new WebQuery().orderAsc("id").fetch("id")));
	}


	@Test
	public void testLogSQL() throws Exception
	{
		ParentEntity obj1 = new ParentEntity();
		obj1.setName("Name1");
		dao.save(obj1);

		ParentEntity obj2 = new ParentEntity();
		obj2.setName("Name2");
		dao.save(obj2);

		final ConstrainedResultSet<ParentEntity> resultset = dao.findByUriQuery(new WebQuery().orderAsc("id").limit(1000).logSQL(true));

		assertEquals(getIds(Arrays.asList(obj1, obj2)), getIds(resultset.getList())); // must have the right answer
		assertNotNull(resultset.getSql());

		System.out.println(resultset.getSql());
		assertEquals("Number of SQL statements executed", 2, resultset.getSql().size());
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

		assertEquals(getIds(obj2, obj1), getIds(dao.findByUriQuery(new WebQuery().orderDesc("id")).getList()));
	}


	/**
	 * Tests that computing size while applying ordering and limiting to the resultset still works
	 *
	 * @throws Exception
	 */
	@Test
	public void testComputeSizeWithOrder() throws Exception
	{
		{
			ParentEntity obj1 = new ParentEntity();
			obj1.setName("Name1");
			dao.save(obj1);

			ParentEntity obj2 = new ParentEntity();
			obj2.setName("Name2");
			dao.save(obj2);

			ParentEntity obj3 = new ParentEntity();
			obj3.setName("Name3");
			dao.save(obj3);
		}


		final WebQuery query = new WebQuery().orderDesc("name").limit(2);

		// First, test that the correct results are returned with computeSize=false
		{
			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(query);

			// Must honour limit
			assertEquals("without computeSize; must only have returned 2 results", 2, results.getList().size());
			assertEquals("without computeSize; must have returned right 2 entities",
			             Arrays.asList("Name3", "Name2"),
			             results.getList().stream().map(e -> e.getName()).collect(Collectors.toList()));
		}

		// Next, test that we get the same results with computeSize=true (ensures the order is still present after size is computed)
		query.computeSize(true);
		{
			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(query);

			// Must have correct total size
			assertEquals("with computeSize; must have computed correct total size", Long.valueOf(3), results.getTotal());

			// Must honour limit
			assertEquals("with computeSize; must only have returned 2 results", 2, results.getList().size());
			assertEquals("with computeSize; must have returned right 2 entities",
			             Arrays.asList("Name3", "Name2"),
			             results.getList().stream().map(e -> e.getName()).collect(Collectors.toList()));
		}
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
			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(new WebQuery());

			assertEquals(2, results.getList().size());
		}

		// Search for entries with at least 2 children
		{
			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(new WebQuery().ge("children:size", 2));

			assertEquals(1, results.getList().size());
			assertEquals("Name1", results.getList().get(0).getName());
		}

		// Search for entries with no children
		{
			ConstrainedResultSet<ParentEntity> results = dao.findByUriQuery(new WebQuery().eq("children:size", 0));

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
