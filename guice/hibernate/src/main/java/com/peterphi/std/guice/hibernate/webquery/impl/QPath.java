package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class QPath
{
	private final QPath parent;
	private final String name;
	private final String alias;
	private final QRelation relation;
	private final QProperty property;

	/**
	 * Alias for collections
	 */
	private String hsqlAlias = null;


	public QPath(final QPath parent, final String name, final String alias, final QRelation relation, final QProperty property)
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


	public static QPath parse(final QEntity rootEntity, final QPath parent, final LinkedList<String> segments)
	{
		final QEntity entity;
		if (parent == null)
			entity = rootEntity;
		else
			entity = parent.getRelation().getEntity();

		// Resolve any aliases at this entity level
		entity.fixupPathUsingAliases(segments);

		final String expr = segments.removeFirst();

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

			name = expr.substring(0, expr.indexOf('['));
			alias = expr.substring(expr.indexOf('[') + 1, expr.length() - 1);
		}

		if (entity.hasProperty(name))
			return new QPath(parent, name, alias, null, entity.getProperty(name));
		else if (entity.hasRelation(name))
			return new QPath(parent, name, alias, entity.getRelation(name), null);
		else
		{
			final Set<String> expected = new HashSet<>(entity.getPropertyNames());
			expected.addAll(entity.getRelationNames());
			expected.addAll(entity.getAliasNames());

			throw new IllegalArgumentException("Relationship path error: got " +
			                                   expr +
			                                   "(converted to " +
			                                   name +
			                                   " with alias " +
			                                   alias +
			                                   "), expected one of: " +
			                                   expected);
		}
	}


	public QPath getParent()
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

		QPath current = getParent();

		while (true)
		{
			if (current == null)
			{
				path.add("{alias}"); // root object
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

		QPath qPath = (QPath) o;

		if (parent != null ? !parent.equals(qPath.parent) : qPath.parent != null)
			return false;
		if (name != null ? !name.equals(qPath.name) : qPath.name != null)
			return false;
		return alias != null ? alias.equals(qPath.alias) : qPath.alias == null;
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
