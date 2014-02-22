package com.peterphi.std.types;

/**
 * An immutable type which represents exactly two non-null values, <code>left</code> or <code>right</code>
 *
 * @param <L>
 * 		the left type
 * @param <R>
 * 		the right type
 */
public class Both<L, R>
{
	private final L left;
	private final R right;


	public Both(L left, R right)
	{
		if (left == null)
			throw new IllegalArgumentException("Must provide non-null left!");
		if (right == null)
			throw new IllegalArgumentException("Must provide non-null right!");

		this.left = left;
		this.right = right;
	}


	public L getLeft()
	{
		assert (left != null);
		return left;
	}


	public R getRight()
	{
		assert (right != null);
		return right;
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Both<?, ?> other = (Both<?, ?>) obj;
		if (left == null)
		{
			if (other.left != null)
				return false;
		}
		else if (!left.equals(other.left))
			return false;
		if (right == null)
		{
			if (other.right != null)
				return false;
		}
		else if (!right.equals(other.right))
			return false;
		return true;
	}


	@Override
	public String toString()
	{
		return "[Both left=" + left + ", right=" + right + "]";
	}
}
