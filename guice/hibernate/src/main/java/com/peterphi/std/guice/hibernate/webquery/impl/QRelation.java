package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.common.base.Objects;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQDataType;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntityProperty;

public class QRelation
{
	private final QEntity owner;
	private final String name;
	private final QEntity entity;
	private final boolean nullable;
	private final boolean eager;
	private final boolean collection;


	public QRelation(final QEntity owner,
	                 final String prefix,
	                 final String name,
	                 final QEntity entity,
	                 final boolean nullable,
	                 final boolean eager,
	                 final boolean collection)
	{
		this.owner = owner;

		if (prefix != null)
			this.name = prefix + "." + name;
		else
			this.name = name;

		this.entity = entity;
		this.nullable = nullable;
		this.eager = eager;
		this.collection = collection;
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


	public boolean isEager()
	{
		return eager;
	}


	public boolean isCollection()
	{
		return collection;
	}


	@Override
	public String toString()
	{
		return Objects
				       .toStringHelper(this)
				       .add("owner", owner)
				       .add("name", name)
				       .add("entity", entity)
				       .add("nullable", nullable)
				       .add("eager", eager)
				       .add("collection", collection)
				       .toString();
	}


	public WQEntityProperty encode()
	{
		WQEntityProperty obj = new WQEntityProperty();

		obj.name = this.getName();
		obj.nullable = this.isNullable();
		obj.type = WQDataType.ENTITY;
		obj.relation = getEntity().getName();

		return obj;
	}
}
