package com.peterphi.std.guice.hibernate.webquery.impl;

import java.util.List;

public class QPath
{
	private final List<QRelation> components;
	private final int depth;


	public QPath(final List<QRelation> components, final int depth)
	{
		if (components.isEmpty())
			throw new IllegalArgumentException("Must provide at least one component for a path!");
		else if (depth > components.size())
			throw new IllegalArgumentException("Depth must be <= component list size!");

		this.components = components;
		this.depth = depth;
	}


	public QPath getParent()
	{
		if (depth > 1)
			return new QPath(this.components, depth - 1);
		else
			return null;
	}


	public QRelation getRelation(final int index)
	{
		if (index > depth || index < 0)
			throw new IllegalArgumentException("Illegal value for index: must be between 0 and " + depth);
		else
			return components.get(index);
	}


	public String toJoinAlias()
	{
		return toString('_');
	}


	private String toString(char separator)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < depth; i++)
		{
			if (i != 0)
				sb.append(separator);

			sb.append(components.get(i).getName());
		}

		return sb.toString();
	}


	@Override
	public String toString()
	{
		if (depth == 1)
			return components.get(0).getName();
		else
			return toString('.');
	}


	@Override
	public int hashCode()
	{
		// Copied from AbstractList.hashCode
		int hashCode = 1;

		for (int i = 0; i < depth; i++)
			hashCode = 31 * hashCode + (components.get(i).hashCode());

		return hashCode;
	}


	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		else if (!(o instanceof QPath))
			return false;
		else
		{
			QPath other = (QPath) o;

			// Must have the same depth to be identical
			if (other.depth != this.depth)
				return false;

			for (int i = 0; i < depth; i++)
				if (!other.components.get(i).equals(this.components.get(i)))
					return false; // path mismatch

			// No mismatch encountered
			return true;
		}
	}
}
