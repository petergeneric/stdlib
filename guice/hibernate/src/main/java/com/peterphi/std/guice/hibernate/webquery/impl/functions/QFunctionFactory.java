package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class QFunctionFactory
{
	public static List<QFunction> parse(final QPropertyRef property, List<String> constraints)
	{
		List<QFunction> functions = new ArrayList<>(constraints.size());

		for (String constraint : constraints)
		{
			// TODO merge eq and neq properties?
			functions.add(parse(property, constraint));
		}

		return functions;
	}


	protected static QFunction parse(final QPropertyRef property, String constraint)
	{
		if (constraint.charAt(0) == '_')
		{
			if (constraint.startsWith("_f_"))
			{
				final String[] functionAndParam = getFunctionAndParam(constraint);

				final String function = functionAndParam[0];
				final String param = functionAndParam[1];

				switch (function)
				{
					case "eq":
						return new EqFunction(property, param);
					case "neq":
						return new NeqFunction(property, param);
					case "starts":
						if (property.getProperty().getClazz() != String.class)
							throw new IllegalArgumentException("Can only use function on String properties: " + function);

						return new LikeFunction(property, param + "%");
					case "contains":
						if (property.getProperty().getClazz() != String.class)
							throw new IllegalArgumentException("Can only use function on String properties: " + function);

						return new LikeFunction(property, "%" + param + "%");
					case "range":
						return new RangeFunction(property, param);
					default:
						throw new IllegalArgumentException("Unknown function " + function);
				}
			}
			else if (constraint.equalsIgnoreCase("_null"))
			{
				return new IsNullFunction(property);
			}
			else if (constraint.equalsIgnoreCase("_notnull"))
			{
				return new IsNotNullFunction(property);
			}
			else
			{
				throw new IllegalArgumentException("Unknown constraint: " + constraint);
			}
		}
		else
		{
			// equals
			return new EqFunction(property, constraint);
		}
	}


	protected static String[] getFunctionAndParam(String constraint)
	{
		final String remainder = constraint.substring("_f_".length());

		return StringUtils.split(remainder, "_", 2);
	}
}
