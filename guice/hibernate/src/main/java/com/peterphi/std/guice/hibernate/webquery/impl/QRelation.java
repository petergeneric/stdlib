package com.peterphi.std.guice.hibernate.webquery.impl;

public class QRelation
{
	private final QEntity owner;
	private final String name;
	private final QEntity entity;


	public QRelation(final QEntity owner, final String name, final QEntity entity)
	{
		this.owner = owner;
		this.name = name;
		this.entity = entity;
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
}
