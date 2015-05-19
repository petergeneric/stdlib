package com.peterphi.std.guice.liquibase.hibernate;

import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A filtering {@link ResourceAccessor} implementation that retries unsuccessful absolute resource accesses as relative to /<br
 * />
 * This allows a path like <code>/liquibase/changelog.xml</code> to work within a unit test where it would otherwise need to be
 * written as <code>liquibase/changelog.xml</code>
 */
class RetryAbsoluteAsRelativeResourceAccessor implements ResourceAccessor
{
	private final ResourceAccessor inner;


	public RetryAbsoluteAsRelativeResourceAccessor(final ResourceAccessor inner)
	{
		this.inner = inner;
	}


	/**
	 * Intercepts getResourcesAsStream, the method used to retrieve resources for the master changeset
	 *
	 * @param path
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	@Override
	public Set<InputStream> getResourcesAsStream(final String path) throws IOException
	{
		// First, try the path as specified
		final Set<InputStream> streams = inner.getResourcesAsStream(path);

		// If no results were found and the path was absolute, retry without the leading slash
		if ((streams == null || streams.isEmpty()) && !path.isEmpty() && path.charAt(0) == '/')
		{
			// Strip the leading slash away and re-try the path
			// This lets us
			final String newPath = path.substring(1);

			return inner.getResourcesAsStream(newPath);
		}
		else
		{
			return streams;
		}
	}


	@Override
	public Set<String> list(final String relativeTo,
	                        final String path,
	                        final boolean includeFiles,
	                        final boolean includeDirectories,
	                        final boolean recursive) throws IOException
	{
		// Does not appear to be used

		return inner.list(relativeTo, path, includeFiles, includeDirectories, recursive);
	}


	@Override
	public ClassLoader toClassLoader()
	{
		// Does not appear to be used

		return inner.toClassLoader();
	}
}
