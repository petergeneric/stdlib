package com.peterphi.std.guice.hibernate.webquery.impl;

public class QProperty
{
	private final QEntity entity;
	private final String name;
	private final Class<?> clazz;


	public QProperty(final QEntity entity, final String name, final Class<?> clazz)
	{
		this.entity = entity;
		this.name = name;
		this.clazz = clazz;
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
}
