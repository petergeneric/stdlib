package com.peterphi.std.guice.hibernate.webquery.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HQLFragment
{
	private final String expr;


	public HQLFragment(final String expr)
	{
		this.expr = expr;
	}


	public String toHsqlString(final Map<String, String> expansions)
	{
		return replace(expr, expansions);
	}


	public static HQLFragment combine(final List<HQLFragment> fragments,
	                                  final String prefix,
	                                  final String separator,
	                                  final String suffix)
	{
		// Combine all the expressions
		final String expr = prefix +
		                    fragments
				                    .stream()
				                    .filter(f -> f != null)
				                    .map(f -> f.expr)
				                    .filter(f -> f != null)
				                    .collect(Collectors.joining(separator)) + suffix;

		return new HQLFragment(expr);
	}


	public static String replace(String hql, final Map<String, String> expansions)
	{
		// If there are fragments to expand, expand them
		if (hql.indexOf('{') != -1)
			for (Map.Entry<String, String> expansion : expansions.entrySet())
			{
				hql = hql.replace(expansion.getKey(), expansion.getValue());
			}

		return hql;
	}
}
