package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.List;

class AndGroup implements QFunction
{
	private List<QFunction> constraints;


	public AndGroup(List<QFunction> constraints)
	{
		this.constraints = constraints;
	}


	@Override
	public Criterion encode()
	{
		if (constraints.isEmpty())
			return null;
		else if (constraints.size() == 1)
			return constraints.get(0).encode();
		else
		{
			final Conjunction and = Restrictions.conjunction();

			for (QFunction constraint : constraints)
			{
				Criterion encoded = constraint.encode();

				if (encoded != null)
					and.add(encoded);
			}


			return and;
		}
	}
}
