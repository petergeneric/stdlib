package com.peterphi.std.guice.common.resource.impl;

import com.peterphi.std.guice.common.resource.iface.ResourceNotFoundException;
import com.peterphi.std.guice.common.resource.iface.ResourceProvider;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * An implementation of ResourceProvider that loads resources from a folder on the local filesystem
 */
public class FilesystemResourceProviderImpl implements ResourceProvider
{
	private final File root;

	public FilesystemResourceProviderImpl(File root)
	{
		this.root = root;
	}

	private File getFileForResource(String name)
	{
		// Convert the resource path to a filepath compliant with the local filesystem
		final String relativePath = name.replace('/', File.pathSeparatorChar);

		return new File(root, relativePath);
	}

	@Override
	public InputStream getBinaryResource(String name)
	{
		final File file = getFileForResource(name);

		if (file.exists())
		{
			try
			{
				return new FileInputStream(file);
			}
			catch (Exception e)
			{
				throw new ResourceNotFoundException("Error reading resource from " + file + ": " + e.getMessage(), e);
			}
		}
		else
		{
			throw new ResourceNotFoundException("No such resource '" + name + "' in " + root);
		}
	}

	@Override
	public Reader getTextResource(String name)
	{
		final File file = getFileForResource(name);

		if (file.exists())
		{
			try
			{
				return new FileReader(file);
			}
			catch (Exception e)
			{
				throw new ResourceNotFoundException("Error reading resource from " + file + ": " + e.getMessage(), e);
			}
		}
		else
		{
			throw new ResourceNotFoundException("No such resource '" + name + "' in " + root);
		}
	}

	@Override
	public PropertyFile getPropertyResource(String name)
	{
		final Reader reader = getTextResource(name);

		try
		{
			return new PropertyFile(reader);
		}
		catch (Exception e)
		{
			throw new ResourceNotFoundException("Error loading PropertyFile '" + name + ": " + e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}
	}
}
