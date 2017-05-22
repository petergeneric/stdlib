package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyRef
{
	private final HQPath path;


	public QPropertyRef(final HQPath path)
	{
		if (path.getProperty() == null)
			throw new IllegalArgumentException("Must supply property path! Not " + path);

		this.path = path;
	}


	public HQPath getPath()
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
			return path.toHsqlPath();
	}


	@Override
	public String toString()
	{
		return "QPropertyRef{" + path + '}';
	}
}
