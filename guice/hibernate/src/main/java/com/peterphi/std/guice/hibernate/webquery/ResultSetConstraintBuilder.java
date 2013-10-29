package com.peterphi.std.guice.hibernate.webquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetConstraintBuilder
{
	private Map<String, List<String>> constraints = new HashMap<>();
	private int defaultLimit;


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


	public ResultSetConstraintBuilder replace(String key, Collection<String> values)
	{
		constraints.put(key, new ArrayList<>(values));

		return this;
	}


	public ResultSetConstraintBuilder limit(int limit)
	{
		return replace("_limit", Integer.toString(limit));
	}


	public ResultSetConstraintBuilder offset(int offset)
	{
		return replace("_offset", Integer.toString(offset));
	}


	public ResultSetConstraintBuilder setOrder(String... orders)
	{
		return replace("_order", orders);
	}


	public ResultSetConstraintBuilder addOrder(String... orders)
	{
		return add("_order", orders);
	}


	public ResultSetConstraintBuilder defaultLimit(int limit)
	{
		this.defaultLimit = limit;

		return this;
	}


	public ResultSetConstraint build()
	{
		return new ResultSetConstraint(new HashMap<>(constraints), defaultLimit);
	}
}
