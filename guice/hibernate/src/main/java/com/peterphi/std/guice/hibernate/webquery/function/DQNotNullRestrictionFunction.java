package com.peterphi.std.guice.hibernate.webquery.function;

import com.peterphi.std.guice.hibernate.webquery.DQuery;
import com.peterphi.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.List;

public class DQNotNullRestrictionFunction extends RestrictionFunction
{

	@Override
	public boolean isApplicable(final String queryParamValue)
	{
		return queryParamValue.startsWith("_notnull");
	}
	@Override
	public RestrictionFunctionType getType()
	{
		return RestrictionFunctionType.NOTNULL;
	}

	@Override
	public String getUserInputValue(final String queryParamValue)
	{
		return "NotNull";
	}

	@Override
	void modifyQuery(final DQuery query, String key, List<String> values)
	{
		query.isNotNull(key);
	}
}
