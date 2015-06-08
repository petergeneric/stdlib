package com.peterphi.std.guice.hibernate.webquery.impl;

public class QRelation
{
	private final QEntity owner;
	private final String name;
	private final QEntity entity;
	private final boolean nullable;


	public QRelation(final QEntity owner, final String name, final QEntity entity, final boolean nullable)
	{
		this.owner = owner;
		this.name = name;
		this.entity = entity;
		this.nullable = nullable;
	}


	/**
	 * Get the owning entity of the relation
	 *
	 * @return
	 */
	public QEntity getOwner()
	{
		return owner;
	}


	/**
	 * Get the relation name on the owner
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * Get the entity for the relation destination
	 *
	 * @return
	 */
	public QEntity getEntity()
	{
		return entity;
	}


	/**
	 * Get the nullability of this relationship
	 *
	 * @return
	 */
	public boolean isNullable()
	{
		return nullable;
	}


	@Override
	public String toString()
	{
		return "QRelation{" +
		       "owner=" + owner.getName() +
		       ", name='" + name + '\'' +
		       ", entity=" + entity.getName() +
		       ", nullable=" + nullable +
		       '}';
	}
}
