package com.peterphi.std.guice.hibernate.webquery.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;

public class CriteriaProxy
{
	private final Criteria criteria;
	private final DetachedCriteria detached;


	public CriteriaProxy(final Criteria criteria, final DetachedCriteria detached)
	{
		this.criteria = criteria;
		this.detached = detached;
	}


	public void add(Criterion criterion)
	{
		if (criteria != null)
			criteria.add(criterion);
		else
			detached.add(criterion);
	}


	public void addOrder(Order val)
	{
		if (criteria != null)
			criteria.addOrder(val);
		else
			detached.addOrder(val);
	}


	public void setProjection(Projection val)
	{
		if (criteria != null)
			criteria.setProjection(val);
		else
			detached.setProjection(val);
	}
}
