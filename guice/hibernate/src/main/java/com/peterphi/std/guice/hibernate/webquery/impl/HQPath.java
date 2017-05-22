package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HQPath
{
	public static final String ROOT_OBJECT_ALIAS = "mobj";

	private final HQPath parent;
	private final String name;
	private final String alias;
	private final QRelation relation;
	private final QProperty property;

	/**
	 * Alias for collections
	 */
	private String hsqlAlias = null;


	public HQPath(final HQPath parent, final String name, final String alias, final QRelation relation, final QProperty property)
	{
		this.parent = parent;
		this.name = name;

		if (alias == null && relation != null && relation.isCollection())
			this.alias = UUID.randomUUID().toString(); // Generate a random alias name
		else
			this.alias = alias;

		this.relation = relation;
		this.property = property;
	}


	public static HQPath parse(final QEntity rootEntity, final HQPath parent, final String expr)
	{
		final QEntity entity;
		if (parent == null)
			entity = rootEntity;
		else
			entity = parent.getRelation().getEntity();

		final String name;
		final String alias;
		if (expr.indexOf('[') == -1)
		{
			name = expr;
			alias = null;
		}
		else
		{
			// Should have a ] at the end of the expression
			if (expr.indexOf(']') != expr.length() - 1)
				throw new IllegalArgumentException("Expected segmentName[aliasName]! Got: " + expr);

			name = expr.substring(0, expr.indexOf('[') - 1);
			alias = expr.substring(expr.indexOf('[') + 1, expr.length() - 1);
		}

		if (entity.hasProperty(name))
			return new HQPath(parent, name, alias, null, entity.getProperty(name));
		else if (entity.hasAlias(name))
			throw new IllegalArgumentException("Aliases not currently supported!" +
			                                   name +
			                                   " for " +
			                                   entity +
			                                   " in path " +
			                                   parent);
		else
			return new HQPath(parent, name, alias, entity.getRelation(name), null);
	}


	public HQPath getParent()
	{
		return parent;
	}


	public String getName()
	{
		return name;
	}


	public String getAlias()
	{
		return alias;
	}


	public QRelation getRelation()
	{
		return relation;
	}


	public QProperty getProperty()
	{
		return property;
	}


	public String getHsqlAlias()
	{
		return hsqlAlias;
	}


	public void setHsqlAlias(final String hsqlAlias)
	{
		this.hsqlAlias = hsqlAlias;
	}


	public String toHsqlPath()
	{
		List<String> path = new ArrayList<>();

		path.add(name);

		HQPath current = getParent();

		while (true)
		{
			if (parent == null)
			{
				path.add(ROOT_OBJECT_ALIAS);
				break;
			}
			if (current.getHsqlAlias() != null)
			{
				path.add(current.getHsqlAlias());
				break;
			}
			else
			{
				path.add(current.getName());
			}

			current = current.getParent();
		}

		Collections.reverse(path);

		return path.stream().collect(Collectors.joining("."));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		HQPath hqPath = (HQPath) o;

		if (parent != null ? !parent.equals(hqPath.parent) : hqPath.parent != null)
			return false;
		if (name != null ? !name.equals(hqPath.name) : hqPath.name != null)
			return false;
		return alias != null ? alias.equals(hqPath.alias) : hqPath.alias == null;
	}


	@Override
	public int hashCode()
	{
		int result = parent != null ? parent.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (alias != null ? alias.hashCode() : 0);
		return result;
	}


	@Override
	public String toString()
	{
		return Objects
				       .toStringHelper(this)
				       .add("parent", parent)
				       .add("name", name)
				       .add("alias", alias)
				       .add("relation", relation)
				       .add("property", property)
				       .add("hsqlAlias", hsqlAlias)
				       .toString();
	}
}
