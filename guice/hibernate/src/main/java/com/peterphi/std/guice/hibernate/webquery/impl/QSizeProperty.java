package com.peterphi.std.guice.hibernate.webquery.impl;

/**
 * A special type of property that refers to the size of a collection
 */
public class QSizeProperty extends QProperty
{
	protected final QRelation relation;


	public QSizeProperty(final QRelation relation)
	{
		super(relation.getEntity(), null, relation.getName(), Integer.class, false, false);

		this.relation = relation;
	}


	public boolean isOmitFromSchemaDoc()
	{
		return true; // Consumers must infer this field's existence from an ENTITY property with a collection (since that property has more rich type metadata)
	}

	public QRelation getRelation()
	{
		return relation;
	}


	@Override
	public String toString()
	{
		return "QSizeProperty{" +
		       "relation.name=" +
		       relation.getName() +
		       ", entity.name=" +
		       super.entity.getName() +
		       ", name='" +
		       super.name +
		       '\'' +
		       ", clazz=" +
		       super.clazz +
		       ", nullable=" +
		       super.nullable +
		       '}';
	}
}
