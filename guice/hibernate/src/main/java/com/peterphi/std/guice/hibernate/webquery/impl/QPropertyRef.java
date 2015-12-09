package com.peterphi.std.guice.hibernate.webquery.impl;

public class QPropertyRef
{
	private final QJoin join;
	private final QProperty property;


	public QPropertyRef(final QJoin join, final QProperty property)
	{
		this.join = join;
		this.property = property;
	}


	public QJoin getJoin()
	{
		return join;
	}


	public QProperty getProperty()
	{
		return property;
	}


	public Object parseValue(String value)
	{
		return QTypeHelper.parse(property.getClazz(), value);
	}


	public String getName()
	{
		if (join != null)
			return join.getAlias() + "." + property.getName();
		else
			return property.getName();
	}


	/**
	 * Get a name that can be used in a SQLRestriction
	 *
	 * @return
	 */
	public String getSQLRestrictionName()
	{
		if (join != null)
			return "{" + join.getAlias() + "}." + property.getName();
		else
			return "{alias}." + property.getName();
	}


	@Override
	public String toString()
	{
		return "QPropertyRef{" +
		       "join=" + join +
		       ", property=" + property +
		       '}';
	}
}
