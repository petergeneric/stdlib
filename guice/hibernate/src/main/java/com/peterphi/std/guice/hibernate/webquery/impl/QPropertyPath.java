package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyPath
{
	private final QPath path;
	private final QProperty property;


	public QPropertyPath(final QPath parent, final QProperty property)
	{
		this.path = parent;
		this.property = property;
	}


	public QPath getPath()
	{
		return path;
	}


	public QProperty getProperty()
	{
		return property;
	}


	@Override
	public String toString()
	{
		if (path != null)
			return path.toString() + "." + property.getName();
		else
			return property.getName();
	}
}
