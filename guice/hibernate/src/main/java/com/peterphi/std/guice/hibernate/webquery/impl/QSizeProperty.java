package com.peterphi.std.guice.hibernate.webquery.impl;

public class QSizeProperty extends QProperty
{
	protected final QRelation relation;


	public QSizeProperty(final QRelation relation)
	{
		super(relation.getEntity(), null, relation.getName(), Integer.class, false);

		this.relation = relation;
	}


	public QRelation getRelation()
	{
		return relation;
	}


	@Override
	public String toString()
	{
		return "QSizeProperty{" +
		       "relation=" + relation +
		       "entity.name=" + super.entity.getName() +
		       ", name='" + super.name + '\'' +
		       ", clazz=" + super.clazz +
		       ", nullable=" + super.nullable +
		       '}';
	}
}
