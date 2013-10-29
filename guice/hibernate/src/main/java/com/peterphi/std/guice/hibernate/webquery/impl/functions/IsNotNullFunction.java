package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class IsNotNullFunction implements QFunction
{
	private final QPropertyRef property;


	public IsNotNullFunction(final QPropertyRef property)
	{
		this.property = property;
	}


	@Override
	public Criterion encode()
	{
		return Restrictions.isNotNull(property.getName());
	}
}
