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
		assertEquals("aeeba13f629709df1d69b154f057c38d", DigestUtils.md5Hex(css));
	}
}
