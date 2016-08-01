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
	LT("_f_lt_", true),
	EQ_REF("_f_eqref_", true, true),
	NEQ_REF("_f_neqref_", true, true),
	LE_REF("_f_leref_", true, true),
	LT_REF("_f_ltref_", true, true),
	GE_REF("_f_geref_", true, true),
	GT_REF("_f_gtref_", true, true);

	private final String prefix;
	private final boolean hasParam;
	private final boolean paramIsPropertyRef;


	WQFunctionType(final String prefix, final boolean hasParam, final boolean paramIsPropertyRef)
	{
		this.prefix = prefix;
		this.hasParam = hasParam;
		this.paramIsPropertyRef = paramIsPropertyRef;
	}


	WQFunctionType(final String prefix, final boolean hasParam)
	{
		this(prefix, hasParam, false);
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
