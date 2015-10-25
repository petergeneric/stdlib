package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryConstraintFunction;

import java.util.List;

public class QFunctionFactory
{
	public static QFunction getInstance(QPropertyRef property, WebQueryConstraintFunction function, String param, String param2)
	{
		switch (function)
		{
			case EQ:
				return new Eq(property, param);
			case NEQ:
				return new Neq(property, param);
			case IS_NULL:
				return new IsNull(property);
			case NOT_NULL:
				return new IsNotNull(property);
			case CONTAINS:
				return new Like(property, "%" + param + "%");
			case STARTS_WITH:
				return new Like(property, param + "%");
			case RANGE:
				return new Between(property, param, param2);
			case GE:
				return new Ge(property, param);
			case GT:
				return new Gt(property, param);
			case LE:
				return new Le(property, param);
			case LT:
				return new Lt(property, param);
			default:
				throw new IllegalArgumentException("No mapping for function: " + function);
		}
	}


	public static QFunction and(List<QFunction> constraints)
	{
		return new AndGroup(constraints);
	}


	public static QFunction or(List<QFunction> constraints)
	{
		return new OrGroup(constraints);
	}
}
