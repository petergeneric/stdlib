package com.peterphi.std.guice.common.cache.module;

import com.google.inject.Injector;
import com.peterphi.std.guice.apploader.BasicSetup;
import com.peterphi.std.guice.apploader.impl.GuiceBuilder;
import com.peterphi.std.guice.common.cached.annotation.Cache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheMethodInterceptorTest
{
	@Test
	public void testCache() throws Exception
	{
		Injector injector = new GuiceBuilder().withSetup(new BasicSetup())
		                                      .withNoScannerFactory()
		                                      .withAutoLoadRoles(false)
		                                      .build();

		CacheTest test = injector.getInstance(CacheTest.class);

		//no calls to the test class have been made yet
		assertEquals(0, test.getRealCalls());
		//make a call
		assertEquals(Integer.valueOf(1), test.getValue());
		//one real call has been made
		assertEquals(1, test.getRealCalls());
		//make a call, a cached value should be returned
		assertEquals(Integer.valueOf(1), test.getValue());
		//so the number of real calls is still 1
		assertEquals(1, test.getRealCalls());
		//wait a while (but not long enough for the result to go stale)
		Thread.sleep(500l);
		//make a call, a cached value should be returned
		assertEquals(Integer.valueOf(1), test.getValue());
		//so the number of real calls is still 1
		assertEquals(1, test.getRealCalls());
		//wait a while longer (long enough for the result to go stale)
		Thread.sleep(600l);
		//make a call, a fresh value should be returned
		assertEquals(Integer.valueOf(1), test.getValue());
		//so now the number of real calls to the method is 2
		assertEquals(2, test.getRealCalls());
	}


	public static class CacheTest
	{
		int realCalls = 0;


		public CacheTest()
		{
			realCalls = 0;
		}


		@Cache(timeout = 1000)
		public Integer getValue()
		{
			realCalls++;
			return 1;
		}


		public int getRealCalls()
		{
			return realCalls;
		}
	}
}
