package com.mediasmiths.std.guice.hibernate.webquery;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable object wrapping a query, allowing constraints to be applied using a dotted field style approach (and using String
 * value
 * types) joins dynamically created
 */
public class DQuery
{
	private static final Logger log = Logger.getLogger(DQuery.class);

	private final DQEntity entity;
	private final Criteria criteria;

	/**
	 * Map of dotted path we have created an alias for onto the entity
	 */
	private final Map<String, DQJoin> joins = new HashMap<String, DQJoin>();


	public DQuery(DQEntity entity, Criteria criteria)
	{
		this.entity = entity;
		this.criteria = criteria;
	}


	public void between(String field, List<String> values)
	{
		DQFieldAlias f = getField(field);

		List<Criterion> criterias = new ArrayList<Criterion>();

		for (String value : values)
		{
			final String loHi[] = value.split("\\.\\.", 2);

			final Object lo = f.getType().parse(loHi[0]);
			final Object hi = f.getType().parse(loHi[1]);

			criterias.add(Restrictions.between(f.getName(), lo, hi));
		}

		criteria.add(or(criterias));
	}


	private Criterion or(List<Criterion> criterias)
	{
		if (criterias.size() == 1)
			return criterias.get(0);
		else
			return Restrictions.or(criterias.toArray(new Criterion[criterias.size()]));
	}


	public void eq(String field, List<String> values)
	{
		DQFieldAlias f = getField(field);

		if (values.size() == 1)
			criteria.add(Restrictions.eq(f.getName(), f.getType().parse(values.get(0))));
		else
			criteria.add(Restrictions.in(f.getName(), f.getType().parseAll(values)));
	}


	public void neq(String field, List<String> values)
	{
		DQFieldAlias f = getField(field);

		criteria.add(Restrictions.not(Restrictions.in(f.getName(), f.getType().parseAll(values))));
	}


	public void like(String field, List<String> values)
	{
		DQFieldAlias f = getField(field);

		List<Criterion> criterias = new ArrayList<Criterion>();

		for (Object value : f.getType().parseAll(values))
		{
			criterias.add(Restrictions.like(f.getName(), value));
		}

		criteria.add(or(criterias));
	}


	public void isNull(String field)
	{
		DQFieldAlias f = getField(field);

		criteria.add(Restrictions.isNull(f.getName()));
	}


	public void isNotNull(String field)
	{
		DQFieldAlias f = getField(field);

		criteria.add(Restrictions.isNotNull(f.getName()));
	}


	/**
	 * Translated a dotted field to a DQFieldAlias (optionally transforming the dotted path as part of a join)
	 *
	 * @param field
	 *
	 * @return
	 */
	public DQFieldAlias getField(String field)
	{
		final String parent = getParentField(field);

		if (parent == null)
		{
			// Try for a simple field of the root entity
			DQField f = entity.getField(field);

			if (f != null)
				return new DQFieldAlias(f.getName(), f.getType());
			else
				throw new IllegalArgumentException("Unknown field " + field + " of root entity!");
		}
		else
		{
			// Set up any joins needed to reach the field's parent
			final DQJoin join = joinTo(parent); // TODO we'll need to pass back the alias name...

			final String localField = getLocalField(field);

			final DQField f = join.getEntity().getField(localField);

			if (f != null)
				return new DQFieldAlias(join.getAlias() + "." + f.getName(), f.getType());
			else
				throw new IllegalArgumentException("Unknown field " + field + "!");
		}
	}


	protected DQJoin joinTo(final String field)
	{
		if (joins.containsKey(field))
		{
			return joins.get(field); // already joined, nothing to do
		}

		final String parentName = getParentField(field);

		final DQEntity parent;
		if (parentName != null)
		{
			parent = joinTo(parentName).getEntity(); // Make the join
		}
		else
		{
			parent = this.entity; // we are at the starting object and need to join from there
		}

		final String child = getLocalField(field);

		final DQEntity childField = parent.getEntity(child);

		if (childField == null)
			throw new IllegalArgumentException("Could not create join to " +
			                                   field +
			                                   ", object " +
			                                   parent.getEntityName() +
			                                   " (aliased as " +
			                                   parentName +
			                                   ") does not have relation " +
			                                   child);

		// Remove any dots (hibernate doesn't handle aliases with dots in them)
		final String alias = field.replace('.', '_');

		log.trace("Create alias '" + alias + "' for " + field);
		criteria.createAlias(field,
		                     alias,
		                     JoinType.LEFT_OUTER_JOIN); // We know we've joined to the parent so we must be able to join to one level deeper

		final DQJoin join = new DQJoin(field, alias, childField);

		joins.put(field, join);

		return join;
	}


	protected String getParentField(String field)
	{
		final int lastDot = field.lastIndexOf('.');

		if (lastDot == -1)
			return null; // no parent
		else
			return field.substring(0, lastDot);
	}


	protected String getLocalField(String field)
	{
		final int lastDot = field.lastIndexOf('.');

		if (lastDot == -1)
			return field;
		else
			return field.substring(lastDot + 1);
	}


	public Criteria getCriteria()
	{
		return this.criteria;
	}
}
