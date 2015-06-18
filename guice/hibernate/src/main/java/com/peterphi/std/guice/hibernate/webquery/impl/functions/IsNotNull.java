package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

class IsNotNull implements QFunction
{
	private final QPropertyRef property;


	public IsNotNull(final QPropertyRef property)
	{
		this.property = property;
	}


	@Override
	public Criterion encode()
	{
		return Restrictions.isNotNull(property.getName());
	}
}
