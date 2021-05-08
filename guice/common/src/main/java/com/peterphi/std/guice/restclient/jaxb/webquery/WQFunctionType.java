package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Defines the functions that an be applied to fields (data-type permitting)
 */
@XmlEnum
@XmlType(name = "FunctionType")
public enum WQFunctionType
{
	EQ("_f_eq_", "=", true),
	NEQ("_f_neq_", "!=", true),
	IS_NULL("_null", "is null", false),
	NOT_NULL("_notnull", "IS NOT NULL", false),
	CONTAINS("_f_contains_", "~=", true),
	NOT_STARTS_WITH("_f_nstarts_", "NOT STARTS", true),
	STARTS_WITH("_f_starts_", "STARTS", true),
	RANGE("_f_range_", "BETWEEN", true),
	GE("_f_ge_", ">=", true),
	GT("_f_gt_", ">", true),
	LE("_f_le_", "<=", true),
	LT("_f_lt_", "<", true),
	EQ_REF("_f_eqref_", "EQREF", true, true),
	NEQ_REF("_f_neqref_", "NEQREF", true, true),
	LE_REF("_f_leref_", "LEREF", true, true),
	LT_REF("_f_ltref_", "LTREF", true, true),
	GE_REF("_f_geref_", "GEREF", true, true),
	GT_REF("_f_gtref_", "GTREF", true, true),
	IN("f_in_", "IN", true),
	NOT_IN("f_notin_", "NOT IN", true);

	private final String prefix;
	private final String queryFragForm;
	private final boolean hasParam;
	private final boolean paramIsPropertyRef;


	WQFunctionType(final String prefix, final String queryFragForm, final boolean hasParam, final boolean paramIsPropertyRef)
	{
		this.prefix = prefix;
		this.queryFragForm = queryFragForm;
		this.hasParam = hasParam;
		this.paramIsPropertyRef = paramIsPropertyRef;
	}


	WQFunctionType(final String prefix, final String queryFragForm, final boolean hasParam)
	{
		this(prefix, queryFragForm, hasParam, false);
	}


	public String getPrefix()
	{
		return prefix;
	}


	public String getQueryFragmentForm()
	{
		return this.queryFragForm;
	}


	public boolean hasParam()
	{
		return hasParam;
	}


	public boolean hasBinaryParam()
	{
		return (this == RANGE);
	}


	public boolean hasPropertyRefParam()
	{
		return paramIsPropertyRef;
	}


	public static WQFunctionType getByPrefix(String value)
	{
		for (WQFunctionType function : values())
			if (StringUtils.startsWithIgnoreCase(value, function.getPrefix()))
				return function;

		throw new IllegalArgumentException("Unknown function prefix for: " + value);
	}
}
