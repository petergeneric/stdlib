package com.peterphi.std.guice.hibernate.webquery.function;

import com.peterphi.std.guice.hibernate.webquery.DQuery;
import com.peterphi.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.List;

public class DQRangeRestrictionFunction extends RestrictionFunction
{
	@Override
	public boolean isApplicable(final String queryParamValue)
	{
		return queryParamValue.startsWith("_f_range");
	}

	@Override
	public RestrictionFunctionType getType()
	{
		return RestrictionFunctionType.RANGE;
	}

	@Override
	public String getUserInputValue(final String queryParamValue)
	{
		return queryParamValue.substring("_f_range_".length());
	}

	@Override
	void modifyQuery(final DQuery query, String key, List<String> values)
	{
		query.between(key, values);
	}
}
