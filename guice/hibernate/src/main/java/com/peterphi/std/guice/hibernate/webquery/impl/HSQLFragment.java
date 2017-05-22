package com.peterphi.std.guice.hibernate.webquery.impl;

import java.util.List;
import java.util.stream.Collectors;

public class HSQLFragment
{
	private final String expr;


	public HSQLFragment(final String expr)
	{
		this.expr = expr;
	}

	public String toHsqlString()
	{
		return expr;
	}


	public static HSQLFragment combine(final List<HSQLFragment> fragments,
	                                   final String prefix,
	                                   final String separator,
	                                   final String suffix)
	{
		// Combine all the expressions
		final String expr = prefix +
		                    fragments
				                    .stream()
				                    .filter(f -> f != null)
				                    .map(f -> f.toHsqlString())
				                    .filter(f -> f != null)
				                    .collect(Collectors.joining(separator)) + suffix;

		return new HSQLFragment(expr);
	}
}
