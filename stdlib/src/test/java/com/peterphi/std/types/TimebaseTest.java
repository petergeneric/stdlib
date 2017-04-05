package com.peterphi.std.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class TimebaseTest
{
	@Test
	public void testGetIntSamplesPerSecond() throws Exception
	{
		assertEquals(25, Timebase.HZ_25.getIntSamplesPerSecond());
		assertEquals(50, Timebase.HZ_50.getIntSamplesPerSecond());
		assertEquals(25, new Timebase(2, 50).getIntSamplesPerSecond());
		assertEquals(1000000, Timebase.HZ_1000000.getIntSamplesPerSecond());

		assertEquals(30, Timebase.NTSC.getIntSamplesPerSecond());
	}


	@Test
	public void testGetSamplesPerSecond() throws Exception
	{
		final double delta = 0;

		assertEquals(25, Timebase.HZ_25.getSamplesPerSecond(), delta);
		assertEquals(50, Timebase.HZ_50.getSamplesPerSecond(), delta);
		assertEquals(25, new Timebase(2, 50).getSamplesPerSecond(), delta);
		assertEquals(1000000, Timebase.HZ_1000000.getSamplesPerSecond(), delta);
	}


	@Test
	public void testGetSecondsPerSample() throws Exception
	{
		final double delta = 0;

		assertEquals(0.04D, Timebase.HZ_25.getSecondsPerSample(), delta);
		assertEquals(0.02D, Timebase.HZ_50.getSecondsPerSample(), delta);
		assertEquals(0.04D, new Timebase(2, 50).getSecondsPerSample(), delta);
		assertEquals(0.000001D, Timebase.HZ_1000000.getSecondsPerSample(), delta);
	}


	@Test
	public void testResample() throws Exception
	{
		final double delta = 0; // allow the introduction of no imprecision

		assertEquals(00D, Timebase.HZ_25.resample(0, Timebase.HZ_50), delta); //0@50 = 0@25
		assertEquals(50D, Timebase.HZ_25.resample(100D, Timebase.HZ_50), delta); // 100@50 = 50@25
		assertEquals(100D, Timebase.HZ_50.resample(100D, Timebase.HZ_50), delta); // 100@50 = 100@50
		assertEquals(7D, Timebase.HZ_25.resample(280000D, Timebase.HZ_1000000), delta); // 280000@1M = 7@25
	}


	@Test
	public void testEquals() throws Exception
	{
		assertEquals(Timebase.HZ_25, new Timebase(1, 25));
		assertEquals(Timebase.HZ_50, new Timebase(1, 50));
		assertEquals(new Timebase(2, 50), new Timebase(2, 50));
		assertNotSame(new Timebase(1, 25), new Timebase(2, 50));
	}


	@Test
	public void testToVidispineString() throws Exception
	{
		assertEquals("25", Timebase.HZ_25.toEncodedString());
		assertEquals("50:2", new Timebase(2, 50).toEncodedString());
	}


	@Test
	public void testParseVidispine() throws Exception
	{
		assertEquals(Timebase.HZ_25, Timebase.getInstance("25"));
		assertEquals(new Timebase(2, 50), Timebase.getInstance("50:2"));
	}
}
