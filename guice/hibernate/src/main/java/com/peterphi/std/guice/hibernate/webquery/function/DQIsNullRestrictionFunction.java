package com.peterphi.std.guice.hibernate.webquery.function;

import com.peterphi.std.guice.hibernate.webquery.DQuery;
import com.peterphi.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.List;

public class DQIsNullRestrictionFunction extends RestrictionFunction
{

	@Override
	public boolean isApplicable(final String queryParamValue)
	{
		return queryParamValue.startsWith("_null");
	}

	@Override
	public RestrictionFunctionType getType()
	{
		return RestrictionFunctionType.ISNULL;
	}

	@Override
	public String getUserInputValue(final String queryParamValue)
	{
		return "null";
	}

	@Override
	void modifyQuery(final DQuery query, String key, List<String> values)
	{
		query.isNull(key);
	}
}
