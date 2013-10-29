package com.peterphi.std.guice.hibernate.webquery;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DynamicQueryTest
{
	@Inject
	ShutdownManager shutdownManager;

	@Inject
	HibernateDao<MyObject, Long> dao;

	@Inject
	ResultSetConstraintBuilderFactory builderFactory;


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
		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.parent.name", "Alice");

		// We'd get a org.hibernate.QueryException if Hibernate doesn't understand
		dao.findByUriQuery(builder.build());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testNestedAssociatorThatIsMadeUpDoesNotWork() throws Exception
	{
		ResultSetConstraintBuilder builder = builderFactory.builder();


		builder.add("otherObject.parent.fictionalfield.name", "Alice");

		// Nonsense field shouldn't work
		dao.findByUriQuery(builder.build());
	}


	@Test
	public void testGetByRelationIdIsNull() throws Exception
	{
		MyObject obj = new MyObject();
		obj.setName("Name");
		dao.save(obj);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("otherObject.id", "_null");

		assertEquals(1, dao.findByUriQuery(builder.build()).getList().size());
	}


	@Test
	public void testByBooleanField() throws Exception
	{
		MyObject obj = new MyObject();
		obj.setName("Name");
		obj.setDeprecated(true);
		dao.save(obj);

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "true");

			assertEquals("deprecated=true matches 1", 1, dao.findByUriQuery(builder.build()).getList().size());
		}

		{
			ResultSetConstraintBuilder builder = builderFactory.builder();

			builder.add("deprecated", "false");

			assertEquals("deprecated=false matches nothing", 0, dao.findByUriQuery(builder.build()).getList().size());
		}
	}


	@Test
	public void testOrderAsc() throws Exception
	{
		MyObject obj1 = new MyObject();
		obj1.setName("Name1");
		dao.save(obj1);

		MyObject obj2 = new MyObject();
		obj1.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.addOrder("id asc");

		assertEquals(getIds(Arrays.asList(obj1, obj2)), getIds(dao.findByUriQuery(builder.build()).getList()));
	}


	@Test
	public void testOrderDesc() throws Exception
	{
		MyObject obj1 = new MyObject();
		obj1.setName("Name1");
		dao.save(obj1);

		MyObject obj2 = new MyObject();
		obj1.setName("Name2");
		dao.save(obj2);

		ResultSetConstraintBuilder builder = builderFactory.builder();

		builder.add("_order", Arrays.asList("id desc"));

		assertEquals(getIds(Arrays.asList(obj2, obj1)), getIds(dao.findByUriQuery(builder.build()).getList()));
	}


	private List<Long> getIds(List<MyObject> objs)
	{
		List<Long> list = new ArrayList<>();
		for (MyObject obj : objs)
			list.add(obj.getId());

		return list;
	}
}
