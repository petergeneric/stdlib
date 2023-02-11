package com.peterphi.std.guice.web.rest.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BootstrapStaticResourcesTest
{
	@Test
	public void testGetCSS() throws Exception
	{
		byte[] css = BootstrapStaticResources.get().getCSS();

		assertNotNull(css);
		assertEquals("0385e810f95c1f5c2639ee317c526f7f", DigestUtils.md5Hex(css));
	}
}
