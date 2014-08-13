package com.peterphi.std.guice.hibernate.usertype;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraintBuilderFactory;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Test that a DateTime field marked with a Version annotation gets a new value when an update occurs
 */
public class DateTimeUserTypeVersionFieldTest
{
	@Entity(name = "test_entity")
	private static class ObjWithDateTimeVersionField
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private Long id;

		@Version
		@Column(name = "updated_ts", nullable = false)
		private DateTime lastUpdated = DateTime.now();


		public Long getId()
		{
			return id;
		}


		public void setId(final Long id)
		{
			this.id = id;
		}


		public DateTime getLastUpdated()
		{
			return lastUpdated;
		}


		public void setLastUpdated(final DateTime lastUpdated)
		{
			this.lastUpdated = lastUpdated;
		}
	}

	@Inject
	ShutdownManager shutdownManager;

	@Inject
	HibernateDao<ObjWithDateTimeVersionField, Long> dao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;


	@Before
	public void setUp()
	{
		final Injector injector = new GuiceBuilder().withConfig("hibernate-tests-in-memory-hsqldb.properties")
		                                            .withSetup(new BasicSetup(new HibernateModule()
		                                                       {
			                                                       @Override
			                                                       protected void configure(final Configuration config)
			                                                       {
				                                                       config.addAnnotatedClass(ObjWithDateTimeVersionField.class);
			                                                       }
		                                                       }))
		                                            .build();

		injector.injectMembers(this);
	}


	@After
	public void tearDown()
	{
		shutdownManager.shutdown();
	}


	@Test
	public void testThatDateTimeVersionGetsUpdatedOnStore() throws Exception
	{
		ObjWithDateTimeVersionField obj = new ObjWithDateTimeVersionField();
		final DateTime initialVersion = obj.getLastUpdated();

		final Long id = dao.save(obj);

		obj = dao.getById(id);

		assertEquals("Version should be unmodified for initial store", initialVersion, obj.getLastUpdated());

		dao.update(obj);

		final DateTime nextVersion = obj.getLastUpdated();

		assertNotSame("Version should be changed after update", initialVersion, nextVersion);
		assertTrue("Version date for later update should be later than original time", initialVersion.isBefore(nextVersion));
	}
}
