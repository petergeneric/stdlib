package com.peterphi.std.guice.hibernate.webquery;

import com.google.common.base.MoreObjects;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstrainedResultSet<T>
{
	protected final WebQuery query;
	protected List<String> sql = Collections.emptyList();
	protected List<String> info = Collections.emptyList();
	protected final List<T> list;

	protected Long total;

	public ConstrainedResultSet(WebQuery query, List<T> list)
	{
		if (query == null)
			throw new IllegalArgumentException("Must provide non-null WebQuery!");

		this.query = query;
		this.list = list;
	}


	public <X> ConstrainedResultSet<X> map(final Function<? super T, ? extends X> mapper)
	{
		// Now transform all rows
		final List<X> newList = new ArrayList<>(this.list.size());
		this.list.stream().map(mapper).collect(Collectors.toCollection(() -> newList));

		final ConstrainedResultSet<X> copy = new ConstrainedResultSet<>(this.query, newList);

		copy.total = this.total;

		if (this.sql != null && !this.sql.isEmpty())
			copy.sql = new ArrayList<>(this.sql);
		if (this.info != null && !this.info.isEmpty())
			copy.info = new ArrayList<>(this.info);

		return copy;
	}

	public int getOffset()
	{
			return query.constraints.offset;
	}


	public int getLimit()
	{
			return query.constraints.limit;
	}


	public List<T> getList()
	{
		return list;
	}


	public WebQuery getQuery()
	{
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


	public List<String> getInfo()
	{
		return info;
	}


	public void setInfo(final List<String> info)
	{
		this.info = info;
	}


	/**
	 * When exactly one result is expected, returns that result or throws {@link IllegalArgumentException} if too many or too few
	 * results were returned
	 *
	 * @return
	 */
	public T one()
	{
		final T obj = uniqueResult();

		if (obj != null)
			return obj;
		else
			throw new IllegalArgumentException("Asked for single result but resultset contained 0 results!");
	}


	/**
	 * When a single result is expected, returns that result (or null if no results were returned). Throws an {@link
	 * IllegalArgumentException} if more than one results were returned
	 *
	 * @return
	 */
	public T uniqueResult()
	{
		if (list == null || list.isEmpty())
			return null;
		else if (list.size() == 1)
			return list.get(0);
		else
			throw new IllegalArgumentException("Asked for unique result but resultset contained " + list.size() + " results!");
	}


	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).omitNullValues().add("total", total).add("list", list).toString();
	}
}
