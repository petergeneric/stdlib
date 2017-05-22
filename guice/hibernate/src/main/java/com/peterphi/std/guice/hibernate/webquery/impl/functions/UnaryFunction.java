package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.HSQLFragment;
import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;

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
	public HSQLFragment encode(final HQLEncodingContext ctx)
	{
		return new HSQLFragment(property.toHqlPath() + " " + operator);
	}
}
