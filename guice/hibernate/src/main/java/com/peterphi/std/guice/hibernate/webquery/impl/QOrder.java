package com.peterphi.std.guice.hibernate.webquery.impl;

import org.hibernate.criterion.Order;

class QOrder
{
	private final QPropertyRef property;
	private final boolean asc;


	public QOrder(final QPropertyRef property, final boolean asc)
	{
		this.property = property;
		this.asc = asc;
	}


	public Order encode()
	{
		if (property.getProperty() instanceof QSizeProperty)
		{
			if (asc)
				return SizeOrder.asc(property.toHqlPath());
			else
				return SizeOrder.desc(property.toHqlPath());
		}
		else
		{
			if (asc)
				return Order.asc(property.toHqlPath());
			else
				return Order.desc(property.toHqlPath());
		}
	}
}
