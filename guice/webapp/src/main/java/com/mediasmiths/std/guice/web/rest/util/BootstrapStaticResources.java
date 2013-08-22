package com.mediasmiths.std.guice.web.rest.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class BootstrapStaticResources
{
	public static BootstrapStaticResources get()
	{
		return new BootstrapStaticResources();
	}

	/**
	 * Retrieve static bootstrap source
	 *
	 * @return
	 */
	public String getCSS()
	{
		return getResource("com/mediasmiths/std/guice/web/rest/pagewriter/bootstrap.min.css");
	}

	protected static String getResource(final String resourceName)
	{
		try
		{
			final InputStream is = BootstrapStaticResources.class.getClassLoader().getResourceAsStream(resourceName);

			if (is == null)
				throw new RuntimeException("Could not find resource '" +
				                           resourceName +
				                           "' using ClassLoader for " +
				                           BootstrapStaticResources.class);

			return IOUtils.toString(is, "UTF-8");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load resource '" + resourceName + "'", e);
		}
	}
}
