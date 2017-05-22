package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQFunctionType;

import java.util.ArrayList;
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
				return new BinaryFunction(property, "=", param);
			case NEQ:
				return new BinaryFunction(property, "!=", param);
			case IS_NULL:
				return new UnaryFunction(property, "IS NULL");
			case NOT_NULL:
				return new UnaryFunction(property, "IS NOT NULL");
			case CONTAINS:
				return new BinaryFunction(property, "LIKE", "%" + param + "%");
			case STARTS_WITH:
				return new BinaryFunction(property, "LIKE", param + "%");
			case RANGE:
			{
				// Encode as a composite >= and <=
				GroupFunction and = GroupFunction.and(new ArrayList<>());

				if (param != null)
					and.add(new BinaryFunction(property, ">=", param));
				if (param2 != null)
					and.add(new BinaryFunction(property, "<=", param2));
				return and;
			}
			case GE:
				return new BinaryFunction(property, ">=", param);
			case GT:
				return new BinaryFunction(property, ">", param);
			case LE:
				return new BinaryFunction(property, "<=", param);
			case LT:
				return new BinaryFunction(property, "<", param);

			// Functions referencing a property (can't optimise these so use a PropertyRestriction)
			case EQ_REF:
				return new BinaryPropertyFunction(property, "=", propertyResolver.apply(param));
			case NEQ_REF:
				return new BinaryPropertyFunction(property, "!=", propertyResolver.apply(param));
			case LE_REF:
				return new BinaryPropertyFunction(property, "<=", propertyResolver.apply(param));
			case LT_REF:
				return new BinaryPropertyFunction(property, "<", propertyResolver.apply(param));
			case GE_REF:
				return new BinaryPropertyFunction(property, ">=", propertyResolver.apply(param));
			case GT_REF:
				return new BinaryPropertyFunction(property, ">", propertyResolver.apply(param));
			default:
				throw new IllegalArgumentException("No mapping for function: " + function);
		}
	}


	public static QFunction and(List<QFunction> constraints)
	{
		return GroupFunction.and(constraints);
	}


	public static QFunction or(List<QFunction> constraints)
	{
		return GroupFunction.or(constraints);
	}
}
