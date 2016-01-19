package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.hibernate.webquery.impl.functions.QFunctionFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a Hibernate Criteria
 */
public class QCriteriaBuilder
{
	private final QEntity entity;

	private final Map<QPath, QJoin> joins = new HashMap<>();
	private final List<QFunction> constraints = new ArrayList<>();
	private final List<QOrder> order = new ArrayList<>();

	private final List<String> discriminators = new ArrayList<>();

	private int offset = 0;
	private int limit = 200;


	public QCriteriaBuilder(final QEntity entity)
	{
		this.entity = entity;
	}


	public QCriteriaBuilder limit(int limit)
	{
		this.limit = limit;

		return this;
	}


	public QCriteriaBuilder offset(int offset)
	{
		this.offset = offset;

		return this;
	}


	public void addConstraints(final List<WQConstraintLine> constraints)
	{
		this.constraints.addAll(parseConstraint(constraints));
	}


	private List<QFunction> parseConstraint(List<WQConstraintLine> constraints)
	{
		List<QFunction> list = new ArrayList<>(constraints.size());

		for (WQConstraintLine line : constraints)
		{
			if (line instanceof WQConstraint)
				list.add(parseConstraint((WQConstraint) line));
			else
				list.add(parseConstraint((WQGroup) line));
		}

		return list;
	}


	private QFunction parseConstraint(WQConstraint constraint)
	{
		return QFunctionFactory.getInstance(getProperty(constraint.field),
		                                    constraint.function,
		                                    constraint.value,
		                                    constraint.value2,
		                                    this :: getProperty);
	}


	private QFunction parseConstraint(WQGroup group)
	{
		List<QFunction> contents = parseConstraint(group.constraints);

		switch (group.operator)
		{
			case AND:
				return QFunctionFactory.and(contents);
			case OR:
				return QFunctionFactory.or(contents);
			default:
				throw new IllegalArgumentException("Unknown group operator: " + group.operator);
		}
	}


	public void addClass(final List<String> values)
	{
		this.discriminators.addAll(values);
	}


	/**
	 * Append the Criteria currently described by this object to the provided Criteria<br />
	 * This method will also set the FirstResult and the MaxResults properties of the Criteria.
	 *
	 * @param criteria
	 * 		the base criteria to use
	 */
	public void appendTo(Criteria criteria)
	{
		appendTo(criteria, true, true);
	}


	/**
	 * @param criteria
	 * 		the base criteria to use
	 * @param includeConstraints
	 * 		if true, the regular criteria will be added to the query (all non-discriminator constraints)
	 * @param includePagination
	 * 		if true, LIMIT and OFFSET will be set in the query
	 */
	public void appendTo(Criteria criteria, boolean includeConstraints, boolean includePagination)
	{
		appendJoins(criteria);

		appendDiscriminators(criteria);

		if (includeConstraints)
		{
			// Add the constraints
			for (QFunction constraint : constraints)
				criteria.add(constraint.encode());
		}

		appendOrder(criteria);

		if (includePagination)
		{
			criteria.setFirstResult(offset);
			criteria.setMaxResults(limit);
		}
	}


	public void appendDiscriminators(Criteria criteria)
	{
		// Add the special discriminator value constraint
		if (!discriminators.isEmpty())
		{
			// Translate the discriminators into classes
			final List<Class<?>> classes = getClassesByDiscriminators(discriminators);

			if (classes.size() == 1)
			{
				criteria.add(Restrictions.eq("class", classes.get(0)));
			}
			else
			{
				final Disjunction or = Restrictions.disjunction();

				for (Class<?> clazz : classes)
					or.add(Restrictions.eq("class", clazz));

				criteria.add(or);
			}
		}
	}


	public void appendJoins(Criteria criteria)
	{
		// Set up the joins
		for (QJoin join : joins.values())
			criteria.createAlias(join.getPath().toString(), join.getAlias(), JoinType.LEFT_OUTER_JOIN);
	}


	public void appendOrder(Criteria criteria)
	{
		// Add the order
		for (QOrder order : this.order)
			criteria.addOrder(order.encode());
	}


	/**
	 * Translates the set of string discriminators into entity classes
	 *
	 * @return
	 */
	private List<Class<?>> getClassesByDiscriminators(Collection<String> discriminators)
	{
		Map<String, Class<?>> entitiesByName = new HashMap<>();

		// Prepare a Map of discriminator name -> entity class
		for (QEntity child : entity.getSubEntities())
		{
			entitiesByName.put(child.getDiscriminatorValue(), child.getEntityClass());
		}

		// If the root class isn't abstract then add it to the list of possible discriminators too
		if (!entity.isEntityClassAbstract())
			entitiesByName.put(entity.getDiscriminatorValue(), entity.getEntityClass());

		// Translate the discriminator string values to classes
		List<Class<?>> classes = new ArrayList<>(discriminators.size());
		for (String discriminator : discriminators)
		{
			final Class<?> clazz = entitiesByName.get(discriminator);

			if (clazz != null)
				classes.add(clazz);
			else
				throw new IllegalArgumentException("Invalid class discriminator '" +
				                                   discriminator +
				                                   "', expected one of: " +
				                                   entitiesByName.keySet());
		}

		return classes;
	}


	public QCriteriaBuilder addOrder(QPropertyRef property, boolean asc)
	{
		if (asc)
			order.add(new QOrder(property, true));
		else
			order.add(new QOrder(property, false));

		return this;
	}


	public QPropertyRef getProperty(String path)
	{
		try
		{
			QPropertyPath propertyPath = entity.getPath(path).getPropertyPath();

			final QJoin join = join(propertyPath.getPath());
			final QProperty property = propertyPath.getProperty();

			return new QPropertyRef(join, property);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Error building path for property " + path + ": " + e.getMessage(), e);
		}
	}


	public QJoin join(QPath path)
	{
		if (path == null)
			return null;

		final QJoin parent;

		if (joins.containsKey(path))
		{
			return joins.get(path);
		}
		else
		{
			parent = join(path.getParent());
		}

		final QJoin join = new QJoin(path,
		                             path.toJoinAlias(),
		                             parent != null ? parent.getEntity() : path.getRelation(0).getEntity());

		joins.put(path, join);

		return join;
	}


	public QJoin join(String path)
	{
		final QPropertyPathBuilder builder = entity.getPath(path);

		return join(builder.getPath());
	}
}
