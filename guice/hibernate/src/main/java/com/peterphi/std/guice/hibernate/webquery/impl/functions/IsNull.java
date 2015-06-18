package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

class IsNull implements QFunction
{
	private final QPropertyRef property;


	public IsNull(final QPropertyRef property)
	{
		this.property = property;
	}


	@Override
	public Criterion encode()
	{
		return Restrictions.isNull(property.getName());
	}
}
