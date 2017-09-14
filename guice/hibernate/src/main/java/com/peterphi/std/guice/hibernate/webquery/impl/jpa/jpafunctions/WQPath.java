package com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions;

import com.google.common.base.Objects;

public class WQPath
{
	private final String path;


	public WQPath(final String path)
	{
		this.path = path;
	}


	public WQPathSegment getHead()
	{
		final int lastDot = path.lastIndexOf('.');

		if (lastDot == -1)
			return new WQPathSegment(path);
		else
			return new WQPathSegment(path.substring(lastDot + 1));
	}


	public WQPath getTail()
	{
		final int lastDot = path.lastIndexOf('.');

		if (lastDot != -1)
		{
			final String tail = path.substring(0, lastDot);
			return new WQPath(tail);
		}
		else
		{
			return null; // No tail
		}
	}


	public String getPath()
	{
		return path;
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("path", path).toString();
	}
}
