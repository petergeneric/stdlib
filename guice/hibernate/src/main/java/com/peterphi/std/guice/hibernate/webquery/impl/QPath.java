package com.peterphi.std.guice.hibernate.webquery.impl;

import java.lang.ref.WeakReference;

public class QPath implements Cloneable
{
	private final QRelation relation;
	private QPath child;

	private WeakReference<String> path = null;


	public QPath(final QRelation relation, final QPath child)
	{
		if (relation == null)
			throw new IllegalArgumentException("Must specify a relation!");

		this.relation = relation;
		this.child = child;
	}


	public QRelation getRelation()
	{
		return relation;
	}


	public QPath getChild()
	{
		return child;
	}


	public String toJoinAlias()
	{
		return toString().replace('.', '_');
	}


	@Override
	public String toString()
	{
		if (child == null)
		{
			return relation.getName();
		}
		else
		{
			// If we have a cached value already then return that
			{
				final String path = (this.path != null) ? this.path.get() : null;

				if (path != null)
					return path;
			}

			// Compute and store the built path
			{
				StringBuilder sb = new StringBuilder();

				QPath node = this;
				for (int i = 0; node != null; i++)
				{
					if (i != 0)
						sb.append('.');

					sb.append(node.getRelation().getName());

					node = node.getChild();
				}

				final String path = sb.toString();
				this.path = new WeakReference<>(path);

				return path;
			}
		}
	}


	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}


	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		else if (!(o instanceof QPath))
			return false;
		else
			return o.toString().equals(toString());
	}


	@Override
	public QPath clone()
	{
		QPath newPath = new QPath(getRelation(), null);

		// Clone the child
		if (getChild() != null)
			newPath.withChild(getChild().clone());

		return newPath;
	}


	public void withChild(final QPath child)
	{
		if (this.child != null)
			throw new IllegalArgumentException("Cannot set child: already set!");
		else
			this.child = child;

		// invalidate the cached toString value
		this.path = null;
	}


	public void withNoChild()
	{
		this.child = null;

		// invalidate the cached toString value
		this.path = null;
	}


	public QPath getParentOfLeaf()
	{
		if (getChild() == null)
			return null; // we are already a leaf node
		else if (getChild().getChild() == null)
			return this;
		else
			return getChild().getParentOfLeaf();
	}
}
