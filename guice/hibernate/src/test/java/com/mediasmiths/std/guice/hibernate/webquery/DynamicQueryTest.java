package com.mediasmiths.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import com.mediasmiths.std.io.PropertyFile;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DynamicQueryTest
{
	@Inject
	ShutdownManager shutdownManager;

	@Inject
	HibernateDao<MyObject, Long> dao;


	@Before
	public void setUp()
	{
		PropertyFile props = PropertyFile.find("hibernate-tests-in-memory-hsqldb.properties");

		final Injector injector = GuiceInjectorBootstrap.createInjector(props, new BasicSetup(new HibernateModule()
		{
			@Override
			protected void configure(final Configuration config)
			{
				config.addAnnotatedClass(MyObject.class);
				config.addAnnotatedClass(MyOtherObject.class);
			}
		}));

		injector.injectMembers(this);
	}


	@After
	public void tearDown()
	{
		shutdownManager.shutdown();
	}


	@Test
	public void testNestedAssociatorConstraintWorks() throws Exception
	{
		Map<String, List<String>> constraints = new HashMap<String, List<String>>();

		constraints.put("otherObject.parent.name", Arrays.asList("Alice"));

		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(new ResultSetConstraint(constraints, 200));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNestedAssociatorThatIsMadeUpDoesNotWork() throws Exception
	{
		Map<String, List<String>> constraints = new HashMap<String, List<String>>();

		constraints.put("otherObject.parent.fictionalfield.name", Arrays.asList("Alice"));

		// Nonsense field shouldn't work
		dao.findByUriQuery(new ResultSetConstraint(constraints, 200));
	}


	@Test
	public void testGetByRelationIdIsNull() throws Exception
	{
		MyObject obj = new MyObject();
		obj.setName("Name");
		dao.save(obj);

		Map<String, List<String>> constraints = new HashMap<String, List<String>>();

		constraints.put("otherObject.id", Arrays.asList("_null"));

		// Nonsense field shouldn't work
		assertEquals(1, dao.findByUriQuery(new ResultSetConstraint(constraints, 200)).getList().size());
	}
}
