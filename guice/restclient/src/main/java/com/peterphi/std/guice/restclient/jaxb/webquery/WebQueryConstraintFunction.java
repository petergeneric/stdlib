package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringUtils;

/**
 * Defines the functions that an be applied to fields (data-type permitting)
 */
public enum WebQueryConstraintFunction
{
	EQ("_f_eq_", true),
	NEQ("_f_neq_", true),
	IS_NULL("_null", false),
	NOT_NULL("_notnull", false),
	CONTAINS("_f_contains_", true),
	STARTS_WITH("_f_starts_", true),
	RANGE("_f_range_", true),
	GE("_f_ge_", true),
	GT("_f_gt_", true),
	LE("_f_le_", true),
	LT("_f_lt_", true);

	private final String prefix;
	private final boolean hasParam;


	WebQueryConstraintFunction(final String prefix, final boolean hasParam)
	{
		this.prefix = prefix;
		this.hasParam = hasParam;
	}


	public String getPrefix()
	{
		return prefix;
	}


	public boolean hasParam()
	{
		return hasParam;
	}


	public boolean hasBinaryParam()
	{
		return (this == RANGE);
	}


	public static WebQueryConstraintFunction getByPrefix(String value)
	{
		for (WebQueryConstraintFunction function : values())
			if (StringUtils.startsWithIgnoreCase(value, function.getPrefix()))
				return function;

		throw new IllegalArgumentException("Unknown function prefix for: " + value);
	}

}
