package com.mediasmiths.std.threading.retry;

import com.mediasmiths.std.threading.retry.backoff.NoBackoffStrategy;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RetryManagerTest
{
	/**
	 * Test that we only try maxAttempts times at most
	 *
	 * @throws Exception
	 */
	@Test
	public void testMaxAttempts() throws Exception
	{
		final AtomicInteger val = new AtomicInteger();

		// Try at most 3 times
		RetryManager mgr = new RetryManager(new NoBackoffStrategy(), 3);

		final Exception ex = new Exception("boom");

		try
		{
			mgr.runUnchecked(new Retryable<Void>()
			{
				public Void attempt(int attempt) throws Exception
				{
					val.incrementAndGet();

					throw ex;
				}

				public boolean shouldRetry(int attempt, Throwable e)
				{
					return true; // always retry
				}
			});

			fail("Execution should not reach here");
		}
		catch (RuntimeException e)
		{
			// expected
			assertEquals(ex, e.getCause());
		}


		assertEquals(3, val.get());
	}
}
