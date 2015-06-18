package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import com.peterphi.std.guice.hibernate.webquery.impl.QSizeProperty;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

class Between implements QFunction
{
	private final QPropertyRef property;

	private final Object from;
	private final Object to;


	public Between(final QPropertyRef property, final String value)
	{
		this.property = property;

		try
		{
			final String[] bounds = StringUtils.splitByWholeSeparator(value, "..", 2);

			if (bounds.length == 2)
			{
				from = property.parseValue(bounds[0]);

				if (StringUtils.isNotEmpty(bounds[1]))
					to = property.parseValue(bounds[1]);
				else
					to = null;
			}
			else if (bounds.length == 1)
			{
				if (!value.startsWith(".."))
					throw new RuntimeException("Assertion failed: should start with ..");

				from = null;
				to = property.parseValue(bounds[0]);
			}
			else
			{
				throw new IllegalArgumentException("Unexpected format!");
			}
		}
		catch (RuntimeException e)
		{
			throw new IllegalArgumentException("Expected form: [min value]..[max value], not: " +
			                                   value +
			                                   ". Error: " +
			                                   e.getMessage(), e);
		}
	}


	@Override
	public Criterion encode()
	{
		// Special-case constraints on size properties
		if (property.getProperty() instanceof QSizeProperty)
		{
			// N.B. build from other primitives to take advantage of optimisations for > 0, < 1 etc.
			if (from != null && to != null)
				return Restrictions.and(Restrictions.sizeGe(property.getName(), (Integer) from),
				                        Restrictions.sizeLe(property.getName(), (Integer) to));
			else if (from != null)
				return new Ge(property, from.toString()).encode();
			else
				return new Le(property, to.toString()).encode();
		}
		else
		{
			if (from != null && to != null)
				return Restrictions.between(property.getName(), from, to);
			else if (from != null)
				return Restrictions.ge(property.getName(), from);
			else
				return Restrictions.le(property.getName(), to);
		}
	}
}
