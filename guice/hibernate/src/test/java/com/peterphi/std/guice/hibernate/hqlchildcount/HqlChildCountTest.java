package com.peterphi.std.guice.hibernate.hqlchildcount;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties", classPackages = ParentEntity.class)
public class HqlChildCountTest
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

		criteria();
	}


	@Test
	public void testChildHQL() throws Exception
	{
		load();

		query();
	}


	//@Transactional
	public void criteria()
	{
		final ConstrainedResultSet<ParentEntity> resultset = dao.findByUriQuery(new WebQuery()
				                                                                        .dbfetch("children", "children.parent")
				                                                                        .eq("children.flag", true)
				                                                                        .logSQL(true));

		System.out.println("SQL: " + resultset.getSql());
		System.out.println("SQL Statements: " + resultset.getSql().size());

		List<ParentEntity> results = resultset.getList();

		System.out.println(results);

		for (ParentEntity result : results)
		{
			System.out.println(result.getId() +
			                   " - children " +
			                   result.getChildren().stream().map(c -> c.getId().toString()).collect(Collectors.joining(",")));

			assertEquals("each parent should have 3 children", 3, result.getChildren().size());
		}

		assertEquals("should be 2 parent entities", 2, results.size());
	}


	@Transactional
	public void load()
	{
		{
			// TODO populate db
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
			// TODO populate db
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


	@Transactional
	public void query()
	{

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
