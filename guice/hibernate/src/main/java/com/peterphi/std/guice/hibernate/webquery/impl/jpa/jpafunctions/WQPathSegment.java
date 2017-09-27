package com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions;

import com.google.common.base.Objects;

public class WQPathSegment
{
	private final String segment;


	public WQPathSegment(final String segment)
	{
		this.segment = segment;
	}


	public String getPath()
	{
		final int firstOpenSquareBracket = segment.indexOf('[');

		if (firstOpenSquareBracket != -1)
			return segment.substring(0, firstOpenSquareBracket);
		else
			return segment;
	}


	public String getAlias()
	{
		final int firstOpenSquareBracket = segment.indexOf('[');

		if (firstOpenSquareBracket == -1)
			return "[" + segment + "]"; // TODO should this be randomly generated per WQPathSegment?
		else
			return segment.substring(firstOpenSquareBracket);
	}


	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).add("segment", segment).toString();
	}
}
