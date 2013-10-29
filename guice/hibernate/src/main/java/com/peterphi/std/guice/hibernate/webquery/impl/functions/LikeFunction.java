package com.peterphi.std.guice.hibernate.webquery.impl.functions;

import com.peterphi.std.guice.hibernate.webquery.impl.QFunction;
import com.peterphi.std.guice.hibernate.webquery.impl.QPropertyRef;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class LikeFunction implements QFunction
{
	private final QPropertyRef property;
	private final String param;


	public LikeFunction(final QPropertyRef property, final String param)
	{
		this.property = property;
		this.param = param;
	}


	@Override
	public Criterion encode()
	{
		return Restrictions.like(property.getName(), param + "%");
	}
}
