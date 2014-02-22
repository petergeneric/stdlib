package com.peterphi.std.types;

import java.io.Serializable;

/**
 * An abstract base type for a case-sensitive identifier<br />
 * All implementations must directly extend this type (ie. all people who extend this type should mark their implementation as
 * final)<br />
 * It is not permitted to add any additional fields to an Id field: the sole benefit of extending an Id type is to provide clarity
 * to what kind of Id something is<br />
 * An id is never permitted to be null, nor may it be an empty String
 */
public abstract class Id implements Comparable<Id>, Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * If true the constructor for Id will verify that all Ids comply with the
	 */
	public static final boolean SANITY_CHECK = false;

	/**
	 * The internal value of this id; while public, this should only be required very rarely
	 */
	public final String id;


	/**
	 * Constructs a new id<br />
	 *
	 * @param id
	 * 		the id value to use (may not be null)
	 */
	public Id(final String id)
	{
		if (SANITY_CHECK)
		{
			if (this.getClass().getSuperclass() != Id.class)
				throw new IllegalStateException("Any Id type must directly extend Id.class!");

			// Make sure the implementation doesn't add any fields
			if (this.getClass().getFields().length != Id.class.getFields().length)
			{
				throw new IllegalStateException("It is not permitted to add fields to an Id type!");
			}
		}

		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("An id cannot be null or empty!");

		this.id = id;
	}


	@Override
	public final String toString()
	{
		return this.getClass().getSimpleName() + " " + id;
	}


	@Override
	public final boolean equals(final Object o)
	{

		if (this == o)
		{
			return true;
		}
		else if (o == null)
		{
			return false;
		}
		else if (o.getClass().equals(this.getClass()))
		{
			final Id that = (Id) o;

			return this.id.compareTo(that.id) == 0;
		}
		else
		{
			return false;
		}
	}


	/**
	 * Compares another Id to this id. Two ids are only comparable if they have the exact same type: if their types are
	 * incompatible then a ClassCastException will be thrown<br />
	 * If the types are comparable then the result is a case-sensitive comparison of the underlying id valued
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(final Id that)
	{
		if (!that.getClass().equals(this.getClass()))
		{
			throw new ClassCastException("Incomparable Id types: " + that.getClass() + " being compared to " + this.getClass());
		}
		else
		{
			return this.id.compareTo(that.id);
		}
	}


	@Override
	public final int hashCode()
	{
		return id.hashCode();
	}
}
