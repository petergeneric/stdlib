package com.peterphi.std.guice.common.lifecycle;


import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GuiceLifecycleModuleTest
{
	private static boolean POST_CONSTRUCT_WAS_CALLED = false;

	static class ClassWithStringBinding implements GuiceLifecycleListener
	{
		@Inject
		@Named("xyz")
		String xyz;


		@Override
		public void postConstruct()
		{
			POST_CONSTRUCT_WAS_CALLED = true;
		}
	}

	static class ClassWithBrokenDependency implements GuiceLifecycleListener
	{
		@Inject
		DodgyDependency dodgy;


		@Override
		public void postConstruct()
		{
			POST_CONSTRUCT_WAS_CALLED = true;
		}
	}

	/**
	 * A class whose constructor throws an exception
	 */
	static class DodgyDependency
	{
		@Inject
		DodgyDependency()
		{
			throw new RuntimeException();
		}
	}


	@Test
	public void testPostConstructIsCalled()
	{
		POST_CONSTRUCT_WAS_CALLED = false;

		// Bind the xyz property so that construction will succeed
		Injector injector = Guice.createInjector(new GuiceLifecycleModule(), new AbstractModule()
		{
			@Override
			protected void configure()
			{
				Properties properties = new Properties();
				properties.put("xyz", "some value");

				Names.bindProperties(binder(), properties);
			}
		});

		final ClassWithStringBinding instance = injector.getInstance(ClassWithStringBinding.class);

		assertNotNull("Creation of object should succeed", instance);
		assertEquals("some value", instance.xyz);
		assertTrue("postConstruct should have been called", POST_CONSTRUCT_WAS_CALLED);
	}


	@Test(expected = ConfigurationException.class)
	public void testPostConstructNotCalledOnMissingBinding()
	{
		POST_CONSTRUCT_WAS_CALLED = false;

		Injector injector = Guice.createInjector(new GuiceLifecycleModule());
		try
		{
			final ClassWithStringBinding instance = injector.getInstance(ClassWithStringBinding.class);

			assertNull("Creation of object should fail!", instance);
		}
		finally
		{
			assertFalse("postConstruct should not have been called", POST_CONSTRUCT_WAS_CALLED);
		}
	}


	@Test(expected = ProvisionException.class)
	public void testPostConstructNotCalledOnFailureInDependencyConstructor()
	{
		POST_CONSTRUCT_WAS_CALLED = false;

		Injector injector = Guice.createInjector(new GuiceLifecycleModule());
		try
		{
			final ClassWithBrokenDependency instance = injector.getInstance(ClassWithBrokenDependency.class);

			assertNull("Creation of object should fail!", instance);
		}
		finally
		{
			assertFalse("postConstruct should not have been called", POST_CONSTRUCT_WAS_CALLED);
		}
	}
}
