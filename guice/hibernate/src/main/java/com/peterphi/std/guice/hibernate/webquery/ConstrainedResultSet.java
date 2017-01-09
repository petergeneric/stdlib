package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.Collections;
import java.util.List;

public class ConstrainedResultSet<T>
{
	protected /*final*/ ResultSetConstraint constraint;
	protected /*final*/ WebQuery query;
	protected List<String> sql = Collections.emptyList();
	protected final List<T> list;

	protected Long total;


	public ConstrainedResultSet(ResultSetConstraint constraint, List<T> list)
	{
		if (constraint == null)
			throw new IllegalArgumentException("Must provide non-null ResultSetConstraint!");

		this.constraint = constraint;
		this.query = null;
		this.list = list;
	}


	public ConstrainedResultSet(WebQuery query, List<T> list)
	{
		if (query == null)
			throw new IllegalArgumentException("Must provide non-null ResultSetConstraint!");
		this.constraint = null;
		this.query = query;
		this.list = list;
	}


	public int getOffset()
	{
		if (constraint != null)
			return constraint.getOffset();
		else
			return query.constraints.offset;
	}


	public int getLimit()
	{
		if (constraint != null)
			return constraint.getLimit();
		else
			return query.constraints.limit;
	}


	public List<T> getList()
	{
		return list;
	}


	/**
	 * @return
	 *
	 * @deprecated use WebQueryDefinition instead
	 */
	@Deprecated
	public ResultSetConstraint getConstraint()
	{
		if (constraint == null)
			return new ResultSetConstraint(getQuery().encode());

		return constraint;
	}


	public WebQuery getQuery()
	{
		if (query == null)
			query = constraint.toQuery();

		return query;
	}


	public Long getTotal()
	{
		return total;
	}


	public void setTotal(final Long total)
	{
		this.total = total;
	}


	public List<String> getSql()
	{
		return sql;
	}


	public void setSql(final List<String> sql)
	{
		this.sql = sql;
	}
}
