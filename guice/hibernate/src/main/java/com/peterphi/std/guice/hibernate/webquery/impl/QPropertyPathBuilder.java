package com.peterphi.std.guice.hibernate.webquery.impl;

import java.util.ArrayList;
import java.util.List;

public class QPropertyPathBuilder
{
	private List<QRelation> path = new ArrayList<>();

	private QProperty property;


	public void append(QRelation relation)
	{
		if (property != null)
			throw new IllegalStateException("Cannot append a relation path when a property has already been appended!");

		path.add(relation);
	}


	public void append(QProperty property)
	{
		if (this.property != null)
			throw new IllegalStateException("Cannot append a property twice! Already have " + this.property);

		this.property = property;
	}


	public QPath getPath()
	{
		if (!path.isEmpty())
			return new QPath(path, path.size());
		else
			return null;
	}


	public QPropertyPath getPropertyPath()
	{
		if (property == null)
			throw new IllegalArgumentException("Expected property but path referred to a relation: " + path.toString());

		return new QPropertyPath(getPath(), property);
	}
}
