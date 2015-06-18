package com.peterphi.std.guice.hibernate.webquery.impl;

public class QProperty
{
	protected final QEntity entity;
	protected final String name;
	protected final Class<?> clazz;
	protected final boolean nullable;


	public QProperty(final QEntity entity, final String prefix, final String name, final Class<?> clazz, final boolean nullable)
	{
		this.entity = entity;

		if (prefix != null)
			this.name = prefix + "." + name;
		else
			this.name = name;

		this.clazz = clazz;
		this.nullable = nullable;
	}


	public QEntity getEntity()
	{
		return entity;
	}


	public String getName()
	{
		return name;
	}


	public Class<?> getClazz()
	{
		return clazz;
	}


	public boolean isNullable()
	{
		return nullable;
	}


	@Override
	public String toString()
	{
		return "QProperty{" +
		       "entity.name=" + entity.getName() +
		       ", name='" + name + '\'' +
		       ", clazz=" + clazz +
		       ", nullable=" + nullable +
		       '}';
	}
}
