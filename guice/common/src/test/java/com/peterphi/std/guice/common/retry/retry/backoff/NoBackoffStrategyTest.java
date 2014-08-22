package com.peterphi.std.guice.common.retry.retry.backoff;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoBackoffStrategyTest
{
	@Test
	public void testGetBackoff()
	{
		NoBackoffStrategy svc = new NoBackoffStrategy();

		assertEquals(0, svc.getBackoff(1));
		assertEquals(0, svc.getBackoff(2));
		assertEquals(0, svc.getBackoff(3));
	}
}

