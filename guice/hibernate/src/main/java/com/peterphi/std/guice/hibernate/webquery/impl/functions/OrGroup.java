package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

class OrGroup implements QFunction
{
	private List<QFunction> constraints;


	public OrGroup(List<QFunction> constraints)
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
			final Junction or = Restrictions.disjunction();

			for (QFunction constraint : constraints)
			{
				Criterion encoded = constraint.encode();

				if (encoded != null)
					or.add(encoded);
			}


			return or;
		}
	}
}
