package com.peterphi.std.guice.hibernate.hqlchildcount;

import com.google.inject.Inject;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.testing.GuiceRunner;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceTestSpec;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceRunner.class)
@GuiceTestSpec(config = "hibernate-tests-in-memory-hsqldb.properties",
             classPackages = QEntity.class)
public class HqlChildCountTest
{

	@Inject
	QDao dao;

	@Inject
	HibernateDao<REntity, Long> rDao;


	/**
	 * Simple test that using HQL works
	 *
	 * @throws Exception
	 */
	@Test
	public void testEmptyDb() throws Exception
	{
		List<Long> ids = dao.getIdsByQuery("select q.id from Q q");

		assertEquals(0, ids.size());
	}


	@Test
	public void testFilledDb() throws Exception
	{
		// Find a Q where 0 != count(child.flag=false) AND q.capacity > count(r.flag=true)
		final String query = "SELECT q.id FROM Q q WHERE q.capacity > (SELECT COUNT(r.id) FROM R r WHERE r.parent=q.id AND r.flag=true) AND 0 <> (SELECT COUNT(r.id) FROM R r WHERE r.parent=q.id AND r.flag=false)";

		QEntity q1;
		{
			q1 = new QEntity();
			q1.setCapacity(2);
			q1 = dao.getById(dao.save(q1));
		}

		List<Long> ids;

		ids = dao.getIdsByQuery(query);
		assertEquals(0, ids.size());

		{
			REntity r = new REntity();
			r.setParent(q1);
			r.setFlag(true);

			rDao.save(r);
		}

		ids = dao.getIdsByQuery(query);
		assertEquals(0, ids.size());

		{
			REntity r = new REntity();
			r.setParent(q1);
			r.setFlag(false);

			rDao.save(r);
		}

		// One Q should match now
		ids = dao.getIdsByQuery(query);
		assertEquals(1, ids.size());


		{
			REntity r = new REntity();
			r.setParent(q1);
			r.setFlag(true);

			rDao.save(r);
		}

		// Now none should match again
		ids = dao.getIdsByQuery("SELECT q.id FROM Q q WHERE q.capacity > (SELECT COUNT(r.id) FROM R r WHERE r.parent=q.id AND r.flag=true) AND (SELECT COUNT(r.id) FROM R r WHERE r.parent=q.id AND r.flag=false) <> 0");
		assertEquals(0, ids.size());
	}
}
