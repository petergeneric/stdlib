package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.webquery.impl.functions.QFunctionFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a Hibernate Criteria
 */
class QCriteriaBuilder
{
	private final QEntity entity;

	private final Map<QPath, QJoin> joins = new HashMap<>();
	private final List<QFunction> constraints = new ArrayList<>();
	private final List<QOrder> order = new ArrayList<>();

	private final List<String> discriminators = new ArrayList<>();

	private Integer offset = 0;
	private Integer limit = 200;


	public QCriteriaBuilder(final QEntity entity)
	{
		this.entity = entity;
	}


	public QCriteriaBuilder(final QEntity entity, final WebQuery query)
	{
		this(entity);

		this.offset(query.getOffset());
		this.limit(query.getLimit());

		// Add the sort order
		for (WQOrder order : query.orderings)
			this.addOrder(this.getProperty(order.field), order.isAsc());

		if (StringUtils.isNotBlank(query.constraints.subclass))
			this.addClass(Arrays.asList(query.constraints.subclass.split(",")));

		this.addConstraints(query.constraints.constraints);
	}

	public QCriteriaBuilder clearConstraints()
	{
		this.constraints.clear();

		return this;
	}


	public QCriteriaBuilder clearOrder()
	{
		this.order.clear();

		return this;
	}


	public QCriteriaBuilder clearPagination()
	{
		this.offset = null;
		this.limit = null;

		return this;
	}


	public QCriteriaBuilder limit(Integer limit)
	{
		this.limit = limit;

		return this;
	}


	public QCriteriaBuilder offset(Integer offset)
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
	 * This method will set the FirstResult and the MaxResults properties of the Criteria if they're set in the query.
	 *
	 * @param criteria
	 * 		the base criteria to use
	 */
	public void appendTo(Criteria criteria)
	{
		appendJoins(criteria);

		appendDiscriminators(criteria);

		// Add the constraints
		for (QFunction constraint : constraints)
			throw new NotImplementedException("Moved to HQBuilder, QFunction no longer works this way");
			//criteria.add(constraint.encode());

		appendOrder(criteria);

		if (offset != null)
			criteria.setFirstResult(offset);

		if (limit != null)
			criteria.setMaxResults(limit);
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

			throw new NotImplementedException("Moved to HQBuilder style, QPropertyRef no longer works in this way");
			//return new QPropertyRef(join, property);
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
