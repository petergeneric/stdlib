package com.peterphi.std.guice.hibernate.webquery.impl.hql.functions;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.HQLFragment;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.hql.QPropertyRef;

/**
 * A {@link QFunction} that operates on two function references
 */
class BinaryPropertyFunction implements QFunction
{
	private final QPropertyRef property;
	private final String operator;
	private final QPropertyRef otherProperty;


	public BinaryPropertyFunction(final QPropertyRef property, final String operator, final QPropertyRef otherProperty)
	{
		this.property = property;
		this.operator = operator;
		this.otherProperty = otherProperty;
	}


	@Override
	public HQLFragment encode(final HQLEncodingContext ctx)
	{
		return new HQLFragment(property.toHqlPath() + " " + operator + " " + otherProperty.toHqlPath());
	}
}