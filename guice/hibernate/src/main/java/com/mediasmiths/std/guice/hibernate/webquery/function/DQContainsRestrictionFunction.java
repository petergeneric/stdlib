package com.mediasmiths.std.guice.hibernate.webquery.function;

import com.mediasmiths.std.guice.hibernate.webquery.DQuery;
import com.mediasmiths.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.List;

public class DQContainsRestrictionFunction extends RestrictionFunction
{

	@Override
	public boolean isApplicable(final String queryParamValue)
	{
		return queryParamValue.startsWith("_f_contains");
	}

	@Override
	public RestrictionFunctionType getType()
	{
		return RestrictionFunctionType.CONTAINS;
	}

	@Override
	public String getUserInputValue(final String queryParamValue)
	{
		return "%" + queryParamValue.substring("_f_contains_".length()) + "%";
	}

	@Override
	void modifyQuery(final DQuery query, String key, List<String> values)
	{
		query.like(key, values);
	}

}
