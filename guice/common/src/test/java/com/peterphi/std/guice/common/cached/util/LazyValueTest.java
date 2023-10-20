package com.peterphi.std.guice.common.cached.util;

import org.junit.Test;

import java.util.concurrent.CancellationException;

import static org.junit.Assert.assertEquals;

public class LazyValueTest
{
	@Test
	public void testConstant()
	{
		final var val = LazyValue.of("test");

		assertEquals("isComputed before val requested", true, val.isComputed());
		assertEquals("value", "test", val.get());
		assertEquals("isComputed after val requested", true, val.isComputed());
	}


	@Test
	public void testCancelIsNoopForConstant()
	{
		final var val = LazyValue.of("test");
		val.cancel();
		assertEquals("value", "test", val.get());
	}


	@Test
	public void testSupplier()
	{
		final var val = new LazyValue<>("test" :: toString);

		assertEquals("isComputed before val requested", false, val.isComputed());
		assertEquals("value", "test", val.get());
		assertEquals("isComputed after val requested", true, val.isComputed());
		val.cancel();
		assertEquals("value after post-compute cancel request", "test", val.get());
	}


	@Test(expected = CancellationException.class)
	public void testCancel()
	{
		final var val = new LazyValue<>("test" :: toString);
		val.cancel();
		val.get();
	}
}
