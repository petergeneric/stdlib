package com.peterphi.std.guice.hibernatetest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import com.peterphi.std.guice.hibernate.module.HibernateTransaction;
import com.peterphi.std.guice.hibernate.module.TransactionHelper;
import com.peterphi.std.io.PropertyFile;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

public class DbunitModuleTest
{
	@Inject
	ShutdownManager shutdownManager;

	@Inject
	HibernateDao<SimpleEntity, Long> dao;

	@Inject
	TransactionHelper txutils;

	@Inject
	Provider<QueryDataSet> dataset;


	@Before
	public void setUp()
	{
		PropertyFile props = PropertyFile.find("com/peterphi/std/guice/hibernatetest/hsqldb-in-memory.properties");

		final Injector injector = GuiceInjectorBootstrap.createInjector(props, new BasicSetup(new DbunitModule(),
		                                                                                      new HibernateModule()
		                                                                                      {
			                                                                                      @Override
			                                                                                      protected void configure(final Configuration config)
			                                                                                      {
				                                                                                      config.addAnnotatedClass(SimpleEntity.class);
				                                                                                      config.addAnnotatedClass(GroupEntity.class);
			                                                                                      }
		                                                                                      }
		));

		injector.injectMembers(this);
	}


	@After
	public void tearDown()
	{
		shutdownManager.shutdown();
	}


	@Test
	public void test() throws Exception
	{
		// DB is initially empty
		new DbUnitAssert().assertEquals(new FlatXmlDataSetBuilder().build(new StringReader("<dataset><SimpleEntity/><GroupEntity/><simple_entity_join_table/></dataset>")),
		                                dataset.get());

		// Add some db contents
		try (HibernateTransaction tx = txutils.start().withAutoCommit())
		{
			dao.save(new SimpleEntity(1, "alice", new GroupEntity(1), new GroupEntity(2)));
			dao.save(new SimpleEntity(2, "bob"));
			dao.save(new SimpleEntity(3, "carol"));
			dao.save(new SimpleEntity(4, "dave"));
		}

		// DB should now have 4 rows
		FlatXmlDataSet expected = new FlatXmlDataSetBuilder().build(this.getClass()
		                                                                .getResourceAsStream("/com/peterphi/std/guice/hibernatetest/alice-bob-carol-dave-dataset.xml"));
		new DbUnitAssert().assertEquals(expected, dataset.get());
	}
}
