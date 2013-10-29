package com.peterphi.std.guice.hibernate.webquery.impl;

import org.hibernate.Criteria;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a Hibernate Criteria
 */
public class QCriteriaBuilder
{
	private final QEntity entity;

	private final Map<String, QJoin> joins = new HashMap<>();
	private final List<QConstraints> constraints = new ArrayList<>();
	private final List<QOrder> order = new ArrayList<>();

	private int offset = 0;
	private int limit = 200;


	public QCriteriaBuilder(final QEntity entity)
	{
		this.entity = entity;
	}


	public void addAll(Map<String, List<String>> parameters)
	{
		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
		{
			final String key = entry.getKey();

			if (key.charAt(0) == '_')
			{
				if (key.equals("_offset"))
					offset = Integer.parseInt(entry.getValue().get(0));
				else if (key.equals("_limit"))
					limit = Integer.parseInt(entry.getValue().get(0));
				else if (key.equals("_order"))
					addOrder(entry.getValue());
				else
					throw new IllegalArgumentException("Unknown built-in name: " + key);
			}
			else
			{
				addConstraint(key, entry.getValue());
			}
		}
	}


	/**
	 * Append the Criteria currently described by this object to the provided Criteria<br />
	 * This method will also set the FirstResult and the MaxResults properties of the Criteria.
	 *
	 * @param criteria
	 */
	public void append(Criteria criteria)
	{
		// Set up the joins
		for (QJoin join : joins.values())
			criteria.createAlias(join.getPath(), join.getAlias(), JoinType.LEFT_OUTER_JOIN);

		// Add the constraints
		for (QConstraints constraint : constraints)
			criteria.add(constraint.encode());

		// Add the order
		for (QOrder order : this.order)
			criteria.addOrder(order.encode());

		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
	}


	public void addOrder(List<String> orderings)
	{
		for (String ordering : orderings)
			addOrder(ordering);
	}


	public void addOrder(String ordering)
	{
		if (ordering.indexOf(' ') != -1)
		{
			final String[] pathAndOrder = ordering.split(" ", 2);

			final String sortPath = pathAndOrder[0];
			final String direction = pathAndOrder[1];

			final QPropertyRef property = getProperty(sortPath);

			if (direction.equalsIgnoreCase("asc"))
			{
				order.add(new QOrder(property, true));
				return;
			}
			else if (direction.equalsIgnoreCase("desc"))
			{
				order.add(new QOrder(property, false));
				return;
			}
		}

		throw new IllegalArgumentException("Order expected [property] [asc|desc], but got: " + ordering);
	}


	public void addConstraint(String path, List<String> filters)
	{
		final QPropertyRef ref = getProperty(path);

		constraints.add(new QConstraints(ref, filters));
	}


	protected QPropertyRef getProperty(String path)
	{
		try
		{
			final int index = path.lastIndexOf('.');

			final QJoin join;
			final QProperty property;
			if (index != -1)
			{
				// Dotted path
				final String parent = path.substring(0, index);
				final String name = path.substring(index + 1);

				join = join(parent);
				property = join.getEntity().getProperty(name);
			}
			else
			{
				// Non-dotted path
				join = null;
				property = entity.getProperty(path);
			}

			return new QPropertyRef(join, property);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Error building path for property " + path + ": " + e.getMessage(), e);
		}
	}


	public QJoin join(String path)
	{
		if (joins.containsKey(path))
			return joins.get(path);

		// If this is a dotted path, join to the LHS (which is the parent entity for this join) and store the RHS in name
		// If this is not a dotted path, we are the parent entity for this join (and leave the path untouched)
		final QEntity parent;
		final String name;
		{
			final int index = path.lastIndexOf('.');

			if (index != -1)
			{
				final String parentPath = path.substring(0, index);
				name = path.substring(index + 1);

				parent = join(parentPath).getEntity();
			}
			else
			{
				name = path; // no dots in path
				parent = entity;
			}
		}

		final QRelation relation = parent.getRelation(name);

		final String alias = pathToJoinAlias(path);
		joins.put(path, new QJoin(path, alias, relation.getEntity()));

		return joins.get(path);
	}


	private static String pathToJoinAlias(final String path)
	{
		return path.replace('.', '_');
	}
}
