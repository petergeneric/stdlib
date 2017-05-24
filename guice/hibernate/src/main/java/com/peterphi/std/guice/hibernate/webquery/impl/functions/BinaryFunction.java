package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.HQLFragment;
import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;

class BinaryFunction implements QFunction
{
	private final QPropertyRef property;
	private final String operator;
	private final Object value;


	public BinaryFunction(final QPropertyRef property, final String operator, final String value)
	{
		this.property = property;
		this.operator = operator;
		this.value = property.parseValue(value);
	}


	@Override
	public HQLFragment encode(final HQLEncodingContext ctx)
	{
		return new HQLFragment(property.toHqlPath() + " " + operator + " " + ctx.createPropertyPlaceholder(value));
	}
}
