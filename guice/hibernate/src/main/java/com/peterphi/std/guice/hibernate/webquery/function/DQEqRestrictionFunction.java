package com.peterphi.std.guice.hibernate.webquery.function;

import com.peterphi.std.guice.hibernate.webquery.DQuery;
import com.peterphi.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.List;

public class DQEqRestrictionFunction extends RestrictionFunction
{

	@Override
	public boolean isApplicable(final String queryParamValue)
	{
		return queryParamValue.startsWith("_f_eq_") || !queryParamValue.startsWith("_");
	}

	@Override
	public RestrictionFunctionType getType()
	{
		return RestrictionFunctionType.EQ;
	}

	@Override
	public String getUserInputValue(final String queryParamValue)
	{
		if (queryParamValue.startsWith("_f_eq_"))
			return queryParamValue.substring("_f_eq_".length());
		else
			return queryParamValue;
	}

	@Override
	void modifyQuery(final DQuery query, String key, List<String> values)
	{
		query.eq(key, values);
	}


}
