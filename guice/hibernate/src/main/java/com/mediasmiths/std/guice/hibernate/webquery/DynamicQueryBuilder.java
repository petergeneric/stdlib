package com.mediasmiths.std.guice.hibernate.webquery;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A builder that takes constraints supplied in a Map (e.g. from a webapp's form input) and adds those constraints to a Criteria
 * query
 */
public class DynamicQueryBuilder<T>
{
	private final SessionFactory sessionFactory;
	private final Class<T> clazz;
	private final ClassMetadata classMetadata;

	/**
	 * A map describing all defined property types; if a property is missing from this map it will be looked up using hibernate
	 * ClassMetadata
	 */
	protected Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();

	public DynamicQueryBuilder(final SessionFactory sessionFactory, final Class<T> clazz)
	{
		this.sessionFactory = sessionFactory;
		this.clazz = clazz;

		this.classMetadata = this.sessionFactory.getClassMetadata(this.clazz);
	}

	/**
	 * Populate a hibernate Criteria with the constraints we can understand from <code>constraint</code>.<br />
	 * Populate <code>handled</code> with the field constraints we have applied.
	 *
	 * @param criteria
	 * 		the hibernate Criteria object
	 * @param handled
	 * 		a set of fields that have already been handled. this will be updated with the fields processed by this class
	 * @param constraint
	 * 		the resultset constraint
	 */
	public void populateCriteria(Criteria criteria, Set<String> handled, ResultSetConstraint constraint)
	{
		bindBuiltins(criteria, constraint);
		bindFields(criteria, handled, constraint);
	}

	protected void bindFields(Criteria criteria, Set<String> handled, ResultSetConstraint constraint)
	{
		final Map<String, List<String>> constraints = constraint.getConstraints();

		for (String key : constraints.keySet())
		{
			if (!handled.contains(key))
			{
				final Class<?> type = getType(key);

				if (type != null)
				{
					final List<String> values = constraints.get(key);
					final Criterion restriction = createEqConstraint(key, type, values);

					if (restriction != null)
					{
						criteria.add(restriction);
						handled.add(key);
					}
				}
			}
		}
	}

	protected void bindBuiltins(Criteria criteria, ResultSetConstraint constraint)
	{
		criteria.setFirstResult(constraint.getOffset());
		criteria.setMaxResults(constraint.getLimit());
		criteria.setFetchSize(constraint.getLimit());

		for (Order order : constraint.getOrderings())
			criteria.addOrder(order);
	}

	/**
	 * Reads an input value to some Object of the clazz type.
	 *
	 * @param value
	 * @param desired
	 *
	 * @return
	 */
	protected Object parse(String value, Class desired)
	{
		return DQType.parse(value, desired);
	}

	private Class<?> getType(String key)
	{
		for (String propertyName : propertyTypes.keySet())
		{
			if (propertyName.equalsIgnoreCase(key))
			{
				return propertyTypes.get(key);
			}
		}

		return getType(classMetadata, key);
	}

	/**
	 * Create an eq (or isNull/isNotNull) constraint
	 *
	 * @param property
	 * 		the name of the property to constrain
	 * @param type
	 * 		the resolved type of this property
	 * @param values
	 * 		the property values
	 *
	 * @return
	 */
	protected Criterion createEqConstraint(String property, Class<?> type, List<String> values)
	{
		if (values.size() == 1)
		{
			final String value = first(values);

			if ("_null".equalsIgnoreCase(value))
			{
				return Restrictions.isNull(property);
			}
			else if ("_notnull".equalsIgnoreCase(value))
			{
				return Restrictions.isNotNull(property);
			}
			else
			{
				return Restrictions.eq(property, type);
			}
		}
		else
		{
			final Object[] vals = new Object[values.size()];

			for (int i = 0; i < values.size(); i++)
				vals[i] = parse(values.get(i), type);

			return Restrictions.in(property, vals);
		}
	}

	protected Class<?> getType(ClassMetadata metadata, String key)
	{
		String[] names = metadata.getPropertyNames();

		// TODO understand subproperties using . separators?

		for (int i = 0; i < names.length; i++)
		{
			final String name = names[i];

			if (name.equalsIgnoreCase(key))
			{
				Type type = metadata.getPropertyTypes()[i];

				// TODO throw an exception if type.isCollection ?

				return type.getReturnedClass();
			}
		}

		return null; // unknown
	}

	protected <L> L first(List<L> list)
	{
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}
}
