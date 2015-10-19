package com.peterphi.std.guice.hibernate.webquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetConstraintBuilder
{
	private Map<String, List<String>> constraints = new HashMap<>();

	private int defaultLimit;
	private List<String> defaultOrder = null;
	private String defaultFetch = null;
	private List<String> defaultExpand = null;


	ResultSetConstraintBuilder(int defaultLimit)
	{
		this.defaultLimit = defaultLimit;
	}


	public ResultSetConstraintBuilder add(Map<String, List<String>> constraints)
	{
		for (String key : constraints.keySet())
		{
			add(key, constraints.get(key));
		}

		return this;
	}


	public ResultSetConstraintBuilder add(String key, String... values)
	{
		return add(key, Arrays.asList(values));
	}


	public ResultSetConstraintBuilder add(String key, Collection<String> values)
	{
		List<String> existing = constraints.get(key);

		if (existing == null)
		{
			existing = new ArrayList<>();
			constraints.put(key, existing);
		}

		existing.addAll(values);

		return this;
	}


	public ResultSetConstraintBuilder replace(String key, String... values)
	{
		return replace(key, Arrays.asList(values));
	}


	public ResultSetConstraintBuilder add(WebQuerySpecialField key, String... values)
	{
		add(key.getName(), values);

		return this;
	}


	public ResultSetConstraintBuilder replace(WebQuerySpecialField key, String... values)
	{
		replace(key.getName(), values);

		return this;
	}


	public ResultSetConstraintBuilder replace(String key, Collection<String> values)
	{
		constraints.put(key, new ArrayList<>(values));

		return this;
	}


	public ResultSetConstraintBuilder limit(int limit)
	{
		return replace(WebQuerySpecialField.LIMIT, Integer.toString(limit));
	}


	public ResultSetConstraintBuilder offset(int offset)
	{
		return replace(WebQuerySpecialField.OFFSET, Integer.toString(offset));
	}


	public ResultSetConstraintBuilder setOrder(String... orders)
	{
		return replace(WebQuerySpecialField.ORDER, orders);
	}


	public ResultSetConstraintBuilder addOrder(String... orders)
	{
		return add(WebQuerySpecialField.ORDER, orders);
	}


	public ResultSetConstraintBuilder addExpand(String... expands)
	{
		return add(WebQuerySpecialField.EXPAND, expands);
	}


	public ResultSetConstraintBuilder setExpand(String... expands)
	{
		return replace(WebQuerySpecialField.EXPAND, expands);
	}


	public ResultSetConstraintBuilder setFetch(String fetch)
	{
		return replace(WebQuerySpecialField.FETCH, fetch);
	}


	public ResultSetConstraintBuilder setDefaultFetch(String fetch)
	{
		this.defaultFetch = fetch;

		return this;
	}


	public ResultSetConstraintBuilder setDefaultExpand(String... expands)
	{
		if (expands == null || expands.length == 0)
			this.defaultExpand = null;
		else
			this.defaultExpand = Arrays.asList(expands);

		return this;
	}


	/**
	 * Specify a default ordering which will take effect if no order is specified by the user
	 *
	 * @param orders
	 *
	 * @return
	 */
	public ResultSetConstraintBuilder defaultOrder(String... orders)
	{
		this.defaultOrder = Arrays.asList(orders);

		return this;
	}


	public ResultSetConstraintBuilder defaultLimit(int limit)
	{
		this.defaultLimit = limit;

		return this;
	}


	public ResultSetConstraint build()
	{
		Map<String, List<String>> map = new HashMap<>(constraints);

		applyDefault(WebQuerySpecialField.FETCH, map, defaultFetch);
		applyDefault(WebQuerySpecialField.EXPAND, map, defaultExpand);
		applyDefault(WebQuerySpecialField.ORDER, map, defaultOrder);
		applyDefault(WebQuerySpecialField.OFFSET, map, "0");
		applyDefault(WebQuerySpecialField.LIMIT, map, Integer.toString(defaultLimit));

		return new ResultSetConstraint(map);
	}


	private void applyDefault(WebQuerySpecialField field, Map<String, List<String>> constraints, List<String> defaultValue)
	{
		if (defaultValue == null)
			return;
		if (constraints.containsKey(field.getName()))
			return;
		else
			constraints.put(field.getName(), defaultValue);
	}


	private void applyDefault(WebQuerySpecialField field, Map<String, List<String>> constraints, String defaultValue)
	{
		if (defaultValue == null)
			return;
		else
			applyDefault(field, constraints, Collections.singletonList(defaultValue));
	}
}
