package com.peterphi.std.guice.hibernate.largetable;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "hibernate-tests-in-memory-hsqldb.properties",
		classPackages = LargeTableQueryTest.class)
public class LargeTableQueryTest
{
	@Inject
	MyTestDaoImpl dao;

	@Inject
	HibernateDao<LargeTableComplexPKEntity, SomePrimaryKey> complexPKdao;


	/**
	 * Test that searching works with a simple primary key
	 */
	@Test
	@Transactional
	public void testSearchWorks()
	{
		dao.save(new LargeTableSimplePKEntity("Alice"));
		dao.save(new LargeTableSimplePKEntity("Bob"));
		dao.save(new LargeTableSimplePKEntity("Carol"));
		dao.save(new LargeTableSimplePKEntity("Dave"));
		dao.save(new LargeTableSimplePKEntity("Eve"));

		// Try a regular query
		assertEquals(2, dao.countWithAInName());

		// Now try a web query
		assertEquals(2, dao.findByUriQuery(new WebQuery().contains("name", "a")).getList().size());
	}

	/**
	 * Test that searching works with ordering
	 */
	@Test
	@Transactional
	public void testSearchWorksWithOrder()
	{
		dao.save(new LargeTableSimplePKEntity("Alice"));
		dao.save(new LargeTableSimplePKEntity("Bob"));
		dao.save(new LargeTableSimplePKEntity("Carol"));
		dao.save(new LargeTableSimplePKEntity("Dave"));
		dao.save(new LargeTableSimplePKEntity("Eve"));

		// Now try a web query

		List<LargeTableSimplePKEntity> list = dao.findByUriQuery(new WebQuery().contains("name", "l").orderDesc("name")).getList();

		assertEquals(2,list.size());
		assertEquals("Carol",list.get(0).name);
		assertEquals("Alice",list.get(1).name);
	}




	/**
	 * Test that searching works even with a complex primary key
	 */
	@Test
	@Transactional
	public void testSearchComplexPK()
	{
		complexPKdao.save(new LargeTableComplexPKEntity("Alice"));
		complexPKdao.save(new LargeTableComplexPKEntity("Bob"));
		complexPKdao.save(new LargeTableComplexPKEntity("Carol"));
		complexPKdao.save(new LargeTableComplexPKEntity("Dave"));
		complexPKdao.save(new LargeTableComplexPKEntity("Eve"));

		assertEquals(2, complexPKdao.findByUriQuery(new WebQuery().contains("name", "a")).getList().size());
	}
}
