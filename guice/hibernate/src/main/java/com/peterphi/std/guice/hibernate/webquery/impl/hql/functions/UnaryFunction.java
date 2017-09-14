package com.peterphi.std.guice.hibernate.webquery.impl.hql.functions;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.HQLFragment;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.QPropertyRef;

public class UnaryFunction implements QFunction
{
	private final QPropertyRef property;
	private final String operator;


	public UnaryFunction(final QPropertyRef property, final String operator)
	{
		this.property = property;
		this.operator = operator;
	}


	@Override
	public HQLFragment encode(final HQLEncodingContext ctx)
	{
		return new HQLFragment(property.toHqlPath() + " " + operator);
	}
}
