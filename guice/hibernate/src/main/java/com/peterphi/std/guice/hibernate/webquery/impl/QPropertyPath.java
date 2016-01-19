package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyPath
{
	private final QPath parent;
	private final QProperty property;


	public QPropertyPath(final QPath parent, final QProperty property)
	{
		this.parent = parent;
		this.property = property;
	}


	public QPath getParent()
	{
		return parent;
	}


	public QProperty getProperty()
	{
		return property;
	}


	@Override
	public String toString()
	{
		if (parent != null)
			return parent.toString() + "." + property.getName();
		else
			return property.getName();
	}
}
