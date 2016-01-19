package com.peterphi.std.guice.hibernate.webquery.impl;

class QJoin
{
	private final QPath path;
	private final String alias;
	private final QEntity entity;


	public QJoin(final QPath path, final String alias, final QEntity entity)
	{
		this.path = path;
		this.alias = alias;
		this.entity = entity;
	}


	public QPath getPath()
	{
		return path;
	}


	public String getAlias()
	{
		return alias;
	}


	public QEntity getEntity()
	{
		return entity;
	}


	@Override
	public String toString()
	{
		return "QJoin{" +
		       "path='" + path + '\'' +
		       ", alias='" + alias + '\'' +
		       ", entity=" + entity +
		       '}';
	}
}
