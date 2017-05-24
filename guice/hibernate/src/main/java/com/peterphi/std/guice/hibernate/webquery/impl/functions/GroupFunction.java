package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.HQLEncodingContext;
import com.peterphi.std.guice.hibernate.webquery.impl.HQLFragment;
import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class GroupFunction implements QFunction
{
	private final boolean and;
	private List<QFunction> constraints;


	public GroupFunction(final boolean and)
	{
		this(and, new ArrayList<>());
	}


	public GroupFunction(final boolean and, List<QFunction> constraints)
	{
		this.and = and;
		this.constraints = constraints;
	}


	public GroupFunction add(QFunction function)
	{
		this.constraints.add(function);
		return this;
	}


	@Override
	public HQLFragment encode(HQLEncodingContext ctx)
	{
		if (constraints.isEmpty())
			return null;
		else if (constraints.size() == 1)
			return constraints.get(0).encode(ctx);
		else
		{
			final List<HQLFragment> fragments = constraints
					                                     .stream()
					                                     .filter(c -> c != null)
					                                     .map(c -> c.encode(ctx))
					                                     .collect(Collectors.toList());

			final String separator = and ? " AND " : " OR ";

			return HQLFragment.combine(fragments, "(", separator, ")");
		}
	}


	public static GroupFunction and(List<QFunction> constraints)
	{
		return new GroupFunction(true, constraints);
	}


	public static GroupFunction or(List<QFunction> constraints)
	{
		return new GroupFunction(false, constraints);
	}
}
