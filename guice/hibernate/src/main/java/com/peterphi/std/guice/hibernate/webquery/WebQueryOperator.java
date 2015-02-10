package com.peterphi.std.guice.hibernate.webquery;

public enum WebQueryOperator
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

	private final String shortName;
	private final boolean hasParam;


	WebQueryOperator(final String shortName, final boolean hasParam)
	{
		this.shortName = shortName;
		this.hasParam = hasParam;
	}


	public String getShortName()
	{
		return shortName;
	}


	public boolean hasParam()
	{
		return hasParam;
	}
}
