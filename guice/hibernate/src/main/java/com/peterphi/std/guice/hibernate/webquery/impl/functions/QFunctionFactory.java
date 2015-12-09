package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQFunctionType;

import java.util.List;
import java.util.function.Function;

public class QFunctionFactory
{
	public static QFunction getInstance(QPropertyRef property,
	                                    WQFunctionType function,
	                                    String param,
	                                    String param2,
	                                    Function<String, QPropertyRef> propertyResolver)
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
			// Functions referencing a property (can't optimise these so use a PropertyRestriction)
			case EQ_REF:
			case NEQ_REF:
			case LE_REF:
			case LT_REF:
			case GE_REF:
			case GT_REF:
				QPropertyRef paramRef = propertyResolver.apply(param);

				return new PropertyRestriction(property, function, paramRef);
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
