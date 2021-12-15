package com.peterphi.std.io;

import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PropertyFileTest
{
	@Test
	public void testToMap()
	{
		PropertyFile props = new PropertyFile();
		props.set("a", "b");

		assertEquals(Collections.singletonMap("a", "b"), props.toMap());
	}


	@Test
	public void testToProperties()
	{
		PropertyFile f = new PropertyFile();
		f.set("a", "b");

		Properties properties = new Properties();
		properties.put("a","b");

		assertEquals(properties, f.toProperties());
	}
}
