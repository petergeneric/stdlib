package com.peterphi.std.guice.hibernate.entitycollection;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties", classPackages = ParentEntity.class)
public class EntityCollectionTest
{
	@Inject
	ParentDao dao;

	@Inject
	HibernateDao<ChildEntity, Long> rDao;


	/**
	 * Simple test that using HQL works
	 *
	 * @throws Exception
	 */
	@Test
	public void testEmptyDb() throws Exception
	{
		List<Long> ids = dao.getIdsByQuery("select q.id FROM parent_entity q");

		assertEquals(0, ids.size());
	}


	@Test
	public void testChildCriteria() throws Exception
	{
		load();

		// N.B. not in a Transaction, so whatever's returned from the find call can't be filled in any further
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery().eq("children.flag", true).logSQL(true));

			System.out.println("SQL: " + resultset.getSql());
			System.out.println("SQL Statements: " + resultset.getSql().size());

			List<ParentEntity> results = resultset.getList();

			System.out.println(results);

			assertEquals("Should only need 2 SQL statements", 2, resultset.getSql().size());

			for (ParentEntity result : results)
			{
				System.out.println(result.getId() +
				                   " - children " +
				                   result.getChildren().stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")));

				assertEquals("each parent should have 3 children", 3, result.getChildren().size());
			}

			assertEquals("should be 2 parent entities", 2, results.size());
		}
	}


	@Test
	public void testMultipleAliases() throws Exception
	{
		load();

		// Test fetching with multiple aliases
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery()
					                                                              .eq("children[a].flag", true)
					                                                              .or(or -> or
							                                                                        .isNull("children[b].friend.id")
							                                                                        .neq("children[b].friend.id",
							                                                                             1234)).logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), '\n'));
			System.out.println("SQL Statements: " + resultset.getSql().size());

			assertEquals("Should have 2 result match", 2, resultset.getList().size());
			assertEquals("Should need 2 SQL statements", 2, resultset.getSql().size());

			assertEquals("constraining query should have 2 independent joins to Child",
			             2,
			             StringUtils.countMatches(resultset.getSql().get(0).toLowerCase(), "left outer join child_entity"));
		}
	}


	@Test
	public void testOneExplicitAlias() throws Exception
	{
		load();

		// Test fetching with multiple aliases
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery()
					                                                              .eq("children[a].flag", true)
					                                                              .or(or -> or
							                                                                        .isNull("children[a].friend.id")
							                                                                        .neq("children[a].friend.id",
							                                                                             1234))
					                                                              .logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), '\n'));
			System.out.println("SQL Statements: " + resultset.getSql().size());

			assertEquals("Should have 2 result match", 2, resultset.getList().size());
			assertEquals("Should need 2 SQL statements", 2, resultset.getSql().size());

			assertEquals("constraining query should have 1 join to Child",
			             1,
			             StringUtils.countMatches(resultset.getSql().get(0).toLowerCase(), "left outer join child_entity"));
		}
	}


	@Test
	public void testImplicitAliases() throws Exception
	{
		load();

		// Test fetching with multiple aliases
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery()
					                                                              .eq("children.flag", true)
					                                                              .or(or -> or
							                                                                        .isNull("children.friend.id")
							                                                                        .neq("children.friend.id",
							                                                                             1234))
					                                                              .logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), '\n'));
			System.out.println("SQL Statements: " + resultset.getSql().size());

			assertEquals("Should have 2 result match", 2, resultset.getList().size());
			assertEquals("Should need 2 SQL statements", 2, resultset.getSql().size());

			assertEquals("constraining query should have 1 join to Child",
			             1,
			             StringUtils.countMatches(resultset.getSql().get(0).toLowerCase(), "left outer join child_entity"));
		}
	}


	@Test
	public void testLoadGraphCorrectlyIdentifiesNoCollectionJoins() throws Exception
	{
		load();

		// N.B. not in a Transaction, so whatever's returned from the find call is final
		{
			// Expand a non-collection join
			final ConstrainedResultSet<ChildEntity> resultset = rDao.find(new WebQuery().dbfetch("friend").logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), "\n"));

			assertEquals("should be 1 query", 1, resultset.getSql().size());
		}
	}


	@Test
	public void testLoadGraphWorksWithNoConstraints() throws Exception
	{
		load();

		// N.B. not in a Transaction, so whatever's returned from the find call is final
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery().logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), "\n"));
			System.out.println("SQL Statements: " + resultset.getSql().size());

			List<ParentEntity> results = resultset.getList();

			System.out.println(results);

			assertEquals("Should only need 2 SQL statements (get matching IDs, get entities)", 2, resultset.getSql().size());

			for (ParentEntity result : results)
			{
				System.out.println(result.getId() +
				                   " - children " +
				                   result.getChildren().stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")));

				assertEquals("each parent should have 3 children", 3, result.getChildren().size());
			}

			assertEquals("should be 2 parent entities", 2, results.size());
		}
	}


	@Test
	public void testLoadGraphWorksWithConstraints() throws Exception
	{
		load();

		// N.B. not in a Transaction, so whatever's returned from the find call is final
		{
			final ConstrainedResultSet<ParentEntity> resultset = dao.find(new WebQuery().eq("children.flag", true).logSQL(true));

			System.out.println("SQL: " + StringUtils.join(resultset.getSql(), "\n"));
			System.out.println("SQL Statements: " + resultset.getSql().size());

			List<ParentEntity> results = resultset.getList();

			System.out.println(results);

			assertEquals("Should only need 2 SQL statements", 2, resultset.getSql().size());

			for (ParentEntity result : results)
			{
				System.out.println(result.getId() +
				                   " - children " +
				                   result.getChildren().stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")));

				assertEquals("each parent should have 3 children", 3, result.getChildren().size());
			}

			assertEquals("should be 2 parent entities", 2, results.size());
		}
	}


	@Test
	public void testChildHQL() throws Exception
	{
		load();

		final List<ParentEntity> results = dao.getByQuery(
				"SELECT DISTINCT parent FROM parent_entity parent JOIN FETCH parent.children children JOIN parent.children child WHERE child.flag = true");

		for (ParentEntity result : results)
		{
			System.out.println(result.getId() +
			                   " - children " +
			                   result.getChildren().stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")));
			assertEquals(3, result.getChildren().size());
		}

		assertEquals(2, results.size());
	}


	@Transactional
	public void load()
	{
		{
			ParentEntity p1 = new ParentEntity();
			p1.setCapacity(2);
			p1.setId(dao.save(p1));

			ChildEntity c1 = new ChildEntity();
			c1.setParent(p1);
			c1.setFlag(true);
			c1.setId(rDao.save(c1));

			ChildEntity c2 = new ChildEntity();
			c2.setParent(p1);
			c2.setFlag(true);
			c2.setId(rDao.save(c2));

			ChildEntity c3 = new ChildEntity();
			c3.setParent(p1);
			c3.setFlag(false);
			c3.setId(rDao.save(c3));
		}


		{
			ParentEntity p2 = new ParentEntity();
			p2.setCapacity(2);
			p2.setId(dao.save(p2));

			ChildEntity c1 = new ChildEntity();
			c1.setParent(p2);
			c1.setFlag(true);
			c1.setId(rDao.save(c1));

			ChildEntity c2 = new ChildEntity();
			c2.setParent(p2);
			c2.setFlag(true);
			c2.setId(rDao.save(c2));

			ChildEntity c3 = new ChildEntity();
			c3.setParent(p2);
			c3.setFlag(false);
			c3.setId(rDao.save(c3));
		}
	}


	@Test
	public void testFilledDb() throws Exception
	{
		// Find a Q where 0 != count(child.flag=false) AND q.capacity > count(r.flag=true)
		final String query = "SELECT q.id FROM parent_entity q WHERE q.capacity > (SELECT COUNT(r.id) FROM child_entity r WHERE r.parent=q.id AND r.flag=true) AND 0 <> (SELECT COUNT(r.id) FROM child_entity r WHERE r.parent=q.id AND r.flag=false)";

		ParentEntity p1;
		{
			p1 = new ParentEntity();
			p1.setCapacity(2);
			p1 = dao.getById(dao.save(p1));
		}

		List<Long> ids;

		ids = dao.getIdsByQuery(query);
		assertEquals(0, ids.size());

		{
			ChildEntity r = new ChildEntity();
			r.setParent(p1);
			r.setFlag(true);

			rDao.save(r);
		}

		ids = dao.getIdsByQuery(query);
		assertEquals(0, ids.size());

		{
			ChildEntity r = new ChildEntity();
			r.setParent(p1);
			r.setFlag(false);

			rDao.save(r);
		}

		// One Q should match now
		ids = dao.getIdsByQuery(query);
		assertEquals(1, ids.size());


		{
			ChildEntity r = new ChildEntity();
			r.setParent(p1);
			r.setFlag(true);

			rDao.save(r);
		}

		// Now none should match again

		ids = dao.getIdsByQuery(
				"SELECT q.id FROM parent_entity q WHERE q.capacity > (SELECT COUNT(r.id) FROM child_entity r WHERE r.parent=q.id AND r.flag=true) AND (SELECT COUNT(r.id) FROM child_entity r WHERE r.parent=q.id AND r.flag=false) <> 0");

		assertEquals(0, ids.size());
	}
}
