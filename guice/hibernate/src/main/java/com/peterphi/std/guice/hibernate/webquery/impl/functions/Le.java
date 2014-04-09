package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class Le implements QFunction
{

	private final QPropertyRef property;
	private final Object value;

	public Le(final QPropertyRef property, final String value)
	{
		this.property = property;
		this.value = property.parseValue(value);
	}

	@Override
	public Criterion encode()
	{
		return Restrictions.le(property.getName(), value);
	}
}
