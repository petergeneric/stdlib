package com.peterphi.std.types;

/**
 * An immutable type which represents exactly one non-null value of either <code>left</code> or <code>right</code>
 *
 * @param <L>
 * 		the left type
 * @param <R>
 * 		the right type
 */
@SuppressWarnings({"rawtypes"})
public class Either<L, R>
{
	protected final L left;
	protected final R right;


	/**
	 * @param left
	 * 		optional; if specified then right must be null
	 * @param right
	 * 		optional; if specified then left must be null
	 *
	 * @throws IllegalArgumentException
	 * 		if both are null or both are non-null
	 */
	public Either(L left, R right)
	{
		if (left != null && right != null)
			throw new IllegalArgumentException("Must provide left or right but not both!");
		else if (left != null)
		{
			this.left = left;
			this.right = null;
		}
		else if (right != null)
		{
			this.left = null;
			this.right = right;
		}
		else
		{
			throw new IllegalArgumentException("Must provide left or right!");
		}
	}


	public L getLeft()
	{
		return left;
	}


	public boolean isLeft()
	{
		return left != null;
	}


	public boolean isRight()
	{
		return !isLeft();
	}


	public R getRight()
	{
		return right;
	}


	private final Object getObject()
	{
		if (isLeft())
			return getLeft();
		else
			return getRight();
	}


	@Override
	public int hashCode()
	{
		return getObject().hashCode();
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o == null)
			return false;
		else if (o == this)
			return true;
		else if (o.getClass().equals(this.getClass()))
		{
			final Either that = (Either) o;

			if (this.isLeft() == that.isLeft())
			{
				assert (this.getObject() != null);
				assert (that.getObject() != null);

				return this.getObject().equals(that.getObject());
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}


	@Override
	public String toString()
	{
		if (isLeft())
			return "[" + getClass().getSimpleName() + " left=" + getObject() + "]";
		else
			return "[" + getClass().getSimpleName() + " right=" + getObject() + "]";
	}
}
