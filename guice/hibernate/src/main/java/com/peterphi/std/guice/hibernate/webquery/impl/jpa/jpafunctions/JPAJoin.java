package com.peterphi.std.guice.hibernate.webquery.impl.jpa.jpafunctions;

import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QRelation;
import org.apache.commons.lang.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class JPAJoin
{
	private final CriteriaBuilder builder;
	private final QEntity entity;
	private final From<?, ?> root;
	private final boolean collection;


	public JPAJoin(final CriteriaBuilder builder, final QEntity entity, final From<?, ?> root, final boolean collection)
	{
		this.builder = builder;
		this.entity = entity;
		this.root = root;
		this.collection = collection;
	}


	public JPAJoin(final CriteriaBuilder builder, final QRelation relation, final Join<?, ?> join, final boolean collection)
	{
		this(builder, relation.getEntity(), join, collection);
	}


	public boolean isCollection()
	{
		return collection;
	}


	public JPAJoin join(final String relation)
	{
		final QRelation rel = entity.getRelation(relation);

		final JoinType joinType;

		// For non-nullable non-collection joins, use an INNER join instead of a LEFT join
		if (!rel.isNullable() && !rel.isCollection())
			joinType = JoinType.INNER;
		else
			joinType = JoinType.LEFT;

		final Join<Object, Object> join = root.join(relation, joinType);

		return new JPAJoin(builder, rel, join, rel.isCollection());
	}


	public Expression<?> property(final String name)
	{
		if (name.indexOf(':') == -1)
		{
			entity.getProperty(name); // Validate that such a property exists

			if (StringUtils.equals(name, "class"))
				return root.type();
			else
				return root.get(name);
		}
		else
		{
			// Contains a : so special-case it
			final String[] parts = StringUtils.split(name, ':');

			if (parts.length != 2)
				throw new IllegalArgumentException("Path part may only have one : character! Found: " + name);

			if (StringUtils.equals(parts[1], "size"))
			{
				entity.getProperty(name); // throws an exception if the property provided is not a relation (i.e. a collection)

				return builder.size((Expression) root.get(parts[0]));
			}
			else
			{
				return root.get(parts[0]).get(parts[1]);
			}
		}
	}
}
