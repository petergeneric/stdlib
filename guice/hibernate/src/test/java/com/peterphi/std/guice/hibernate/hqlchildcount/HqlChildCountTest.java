package com.peterphi.std.guice.hibernate.hqlchildcount;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import com.peterphi.std.io.PropertyFile;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class HqlChildCountTest
{
	@Inject
	ShutdownManager shutdownManager;

	@Inject
	QDao dao;

	@Inject
	HibernateDao<REntity, Long> rDao;


	@Before
	public void setUp()
	{
		PropertyFile props = PropertyFile.find("hibernate-tests-in-memory-hsqldb.properties");

		final Injector injector = GuiceInjectorBootstrap.createInjector(props, new BasicSetup(new HibernateModule()
		{
			@Override
			protected void configure(final Configuration config)
			{
				config.addAnnotatedClass(QEntity.class);
				config.addAnnotatedClass(REntity.class);
			}
		}));

		injector.injectMembers(this);
	}


	@After
	public void tearDown()
	{
		shutdownManager.shutdown();
	}


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
