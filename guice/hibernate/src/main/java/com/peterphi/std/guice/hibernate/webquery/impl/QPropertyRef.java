package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyRef
{
	private final QPath path;


	public QPropertyRef(final QPath path)
	{
		if (path.getProperty() == null)
			throw new IllegalArgumentException("Must supply property path! Not " + path);

		this.path = path;
	}


	public QPath getPath()
	{
		return path;
	}


	public QProperty getProperty()
	{
		return path.getProperty();
	}


	public Object parseValue(String value)
	{
		return QTypeHelper.parse(getProperty().getClazz(), value);
	}


	public String toHqlPath()
	{
		if (getProperty() instanceof QSizeProperty)
			return ((QSizeProperty) getProperty()).toHqlPath(path);
		else
			return path.toHsqlPath().replace(':', '.'); // For composite primary keys, turn : into . (e.g. id:timestamp -> id.timestamp as HQL expects)
	}


	@Override
	public String toString()
	{
		return "QPropertyRef{" + path + '}';
	}
}
