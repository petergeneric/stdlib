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
		return new WQPathSegment(getHeadPath());
	}


	public String getHeadPath()
	{
		final int lastDot = path.lastIndexOf('.');

		if (lastDot == -1)
			return path;
		else
			return path.substring(lastDot + 1);
	}


	public WQPath getTail()
	{
		final String tail = getTailPath();

		if (tail != null)
			return new WQPath(tail);
		else
			return null;
	}


	public String getTailPath()
	{
		final int lastDot = path.lastIndexOf('.');

		if (lastDot != -1)
		{
			final String tail = path.substring(0, lastDot);

			return tail;
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
