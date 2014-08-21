package com.peterphi.std.com.peterphi.std.guice.testing.annotations;

import com.google.inject.Inject;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.Automock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Tests the use of the {@link com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.Automock} annotation
 */
@RunWith(GuiceUnit.class)
public class AutomockTest
{
	@Inject
	@Automock
	SomeInterface iface;


	@Before
	public void before()
	{
		Mockito.when(iface.whoami()).thenReturn("hello");
	}


	@Test
	public void x()
	{
		assertEquals("hello", iface.whoami());
	}
}
