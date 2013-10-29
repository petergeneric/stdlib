package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.hibernate.webquery.impl.functions.QFunctionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class QConstraints
{
	private final QPropertyRef property;
	private final List<QFunction> constraints;


	public QConstraints(final QPropertyRef property, List<String> constraints)
	{
		this.property = property;
		this.constraints = QFunctionFactory.parse(property, constraints);
	}


	public QPropertyRef getProperty()
	{
		return property;
	}


	public Criterion encode()
	{
		if (constraints.isEmpty())
			return null;
		else if (constraints.size() == 1)
			return constraints.get(0).encode();
		else
		{
			Junction junction = Restrictions.disjunction();

			for (QFunction constraint : constraints)
			{
				junction.add(constraint.encode());
			}

			return junction;
		}
	}
}
