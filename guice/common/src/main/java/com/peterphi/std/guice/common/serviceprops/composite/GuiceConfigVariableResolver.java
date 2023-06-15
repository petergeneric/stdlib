package com.peterphi.std.guice.common.serviceprops.composite;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.common.cached.CacheManager;
import ognl.Node;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

class GuiceConfigVariableResolver extends StrLookup
{
	/**
	 * OGNL cache
	 */
	private final Cache<String, Node> ognlCache = CacheManager.build("GuiceOgnlConfig",
	                                                                 CacheBuilder.newBuilder().maximumSize(1024).softValues());

	private final GuiceConfig properties;


	GuiceConfigVariableResolver(final GuiceConfig properties)
	{
		this.properties = properties;
	}


	@Override
	public String lookup(final String key)
	{
		if (StringUtils.contains(key, ':'))
		{
			if (StringUtils.startsWith(key, "env:"))
			{
				final String[] parts = key.split(":", 2);

				final String name = parts[1];

				final String resolution = System.getenv(name);
				if (resolution == null)
					return "";
				else
					return resolution;
			}
			else if (StringUtils.startsWith(key, "resource:"))
			{
				final String[] parts = key.split(":", 2);
				final String resource = parts[1];

				try (final InputStream is = getClass().getResourceAsStream(resource))
				{
					return IOUtils.toString(is);
				}
				catch (IOException e)
				{
					throw new RuntimeException("Error reading classpath resource " + resource, e);
				}
			}
			else if (StringUtils.startsWithIgnoreCase(key, "file:"))
			{
				final String[] parts = key.split(":", 2);
				final String file = parts[1];

				try (final FileReader fr = new FileReader(file))
				{
					return IOUtils.toString(fr);
				}
				catch (IOException e)
				{
					throw new RuntimeException("Error reading File " + file, e);
				}
			}
		}

		// Fall back to resolving the property
		return properties.get(key, "");
	}
}
