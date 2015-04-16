package com.peterphi.std.guice.hibernate.webquery;

import java.util.List;

public class ConstrainedResultSet<T>
{
	protected final ResultSetConstraint constraint;
	protected final List<T> list;

	protected Long total;


	public ConstrainedResultSet(ResultSetConstraint constraint, List<T> list)
	{
		this.constraint = constraint;
		this.list = list;
	}


	public int getOffset()
	{
		return constraint.getOffset();
	}


	public int getLimit()
	{
		return constraint.getLimit();
	}


	public List<T> getList()
	{
		return list;
	}


	public ResultSetConstraint getConstraint()
	{
		return constraint;
	}


	public Long getTotal()
	{
		return total;
	}


	public void setTotal(final Long total)
	{
		this.total = total;
	}
}
