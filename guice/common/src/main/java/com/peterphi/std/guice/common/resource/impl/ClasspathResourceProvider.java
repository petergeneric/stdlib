package com.peterphi.std.guice.common.resource.impl;

import com.peterphi.std.guice.common.resource.iface.ResourceNotFoundException;
import com.peterphi.std.guice.common.resource.iface.ResourceProvider;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class ClasspathResourceProvider implements ResourceProvider
{
	private static final String DEFAULT_ENCODING = "UTF-8";
	private final ClassLoader classloader;

	private ClasspathResourceProvider()
	{
		this(ClasspathResourceProvider.class.getClassLoader());
	}

	private ClasspathResourceProvider(ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	@Override
	public InputStream getBinaryResource(String name) throws ResourceNotFoundException
	{
		InputStream is = classloader.getResourceAsStream(name);

		if (is != null)
			return is;
		else
			throw new ResourceNotFoundException("Could not find resource through classpath: " + name);
	}

	@Override
	public Reader getTextResource(String name) throws ResourceNotFoundException
	{
		final InputStream is = getBinaryResource(name);

		return new InputStreamReader(is, Charset.forName(DEFAULT_ENCODING));
	}

	@Override
	public PropertyFile getPropertyResource(String name) throws ResourceNotFoundException
	{
		final Reader reader = getTextResource(name);
		try
		{
			return new PropertyFile(reader);
		}
		catch (IOException e)
		{
			throw new ResourceNotFoundException("Error reading PropertyFile '" + name + "' from classpath: " + e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}
	}
}
