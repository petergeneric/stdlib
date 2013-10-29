package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class RangeFunction implements QFunction
{
	private final QPropertyRef property;

	private final Object from;
	private final Object to;


	public RangeFunction(final QPropertyRef property, final String value)
	{
		this.property = property;

		try
		{
			final String[] bounds = StringUtils.splitByWholeSeparator(value, "..", 2);

			from = property.parseValue(bounds[0]);
			to = property.parseValue(bounds[1]);
		}
		catch (RuntimeException e)
		{
			throw new IllegalArgumentException("Error parsing range: " + value + ": " + e.getMessage(), e);
		}
	}


	@Override
	public Criterion encode()
	{
		return Restrictions.between(property.getName(), from, to);
	}
}
