package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyPathBuilder
{
	private QPath path;
	private QPath leaf;

	private QProperty property;


	public void append(QRelation relation)
	{
		if (property != null)
			throw new IllegalStateException("Cannot append a relation path when a property has already been appended!");

		if (leaf == null)
		{
			path = new QPath(relation, null);
			leaf = path;
		}
		else
		{
			final QPath newPath = new QPath(relation, null);

			leaf.withChild(newPath);

			leaf = newPath;
		}
	}


	public void append(QProperty property)
	{
		if (this.property != null)
			throw new IllegalStateException("Cannot append a property twice! Already have " + this.property);

		this.property = property;
	}


	public QPath getPath()
	{
		return path;
	}


	public QPropertyPath getPropertyPath()
	{
		if (property == null)
			throw new IllegalArgumentException("Expected property but path referred to a relation: " + path.toString());

		return new QPropertyPath(path, property);
	}
}
