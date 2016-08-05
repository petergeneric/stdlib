package com.peterphi.std.io;

import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PropertyFileTest
{
	@Test
	public void testReadOnlyUnionOfPropertyFile()
	{
		PropertyFile a = new PropertyFile();
		a.set("a.file", "a");
		a.set("override-me", "a");

		PropertyFile b = new PropertyFile();
		b.set("b.file", "b");
		b.set("override-me", "b");

		PropertyFile joined = PropertyFile.readOnlyUnion(a, b);

		assertEquals("a", joined.get("a.file"));
		assertEquals("b", joined.get("b.file"));
		assertEquals("b", joined.get("override-me"));
	}


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
