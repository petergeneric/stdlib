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
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalDateUserTypeTest
{
	@Entity(name = "test_entity")
	private static class LocalDateEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public Long id;

		@Column(name = "some_date", nullable = false)
		public LocalDate someDate = new LocalDate();

		@Column(name = "some_date_nullable", nullable = true)
		public LocalDate someNullDate = null;
	}

	@Inject
	ShutdownManager shutdownManager;

	@Inject
	HibernateDao<LocalDateEntity, Long> dao;

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
				                                            config.addAnnotatedClass(LocalDateEntity.class);
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
		LocalDateEntity obj = new LocalDateEntity();
		obj.someDate = new LocalDate("2001-01-01");

		final Long id = dao.save(obj);

		obj = dao.getById(id);

		assertEquals(new LocalDate("2001-01-01"), obj.someDate);
		assertNull(obj.someNullDate);

		// Now make sure we can change the date
		obj.someDate = new LocalDate();

		dao.update(obj);
	}
}
