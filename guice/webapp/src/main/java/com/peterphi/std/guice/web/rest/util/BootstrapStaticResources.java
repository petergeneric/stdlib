package com.peterphi.std.guice.web.rest.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

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
	public byte[] getCSS()
	{
		return getResource("com/peterphi/std/guice/web/rest/pagewriter/bs.css.gz");
	}


	public String getJS()
	{
		return "function setupTheme() {" +
		       "document.documentElement.setAttribute('data-bs-theme', window.matchMedia('(prefers-color-scheme: dark)').matches ?'dark':null);" +
		       "}" +
		       "setupTheme();" +
		       "window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', setupTheme);";
	}

	protected static byte[] getResource(final String resourceName)
	{
		InputStream is = null;
		try
		{
			is = BootstrapStaticResources.class.getClassLoader().getResourceAsStream(resourceName);

			if (is == null)
				throw new RuntimeException("Could not find resource '" +
				                           resourceName +
				                           "' using ClassLoader for " +
				                           BootstrapStaticResources.class);

			// auto decompress gzip resources
			if (resourceName.endsWith(".gz"))
				is = new GZIPInputStream(is);

			return IOUtils.toByteArray(is);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load resource '" + resourceName + "'", e);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}


	public void appendCSS(final StringBuilder sb)
	{
		try
		{
			sb.append(new String(getCSS(), "UTF-8"));
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error parsing bootstrap CSS as UTF-8", e);
		}
	}
}
