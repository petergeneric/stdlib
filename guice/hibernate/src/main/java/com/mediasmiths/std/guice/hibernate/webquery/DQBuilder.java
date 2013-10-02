package com.mediasmiths.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.std.guice.hibernate.webquery.function.RestrictionFunction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * A Dynamic Query Builder that is aware of constraints and Entity types
 */
@Singleton
public class DQBuilder
{
	@Inject
	DQEntityBuilder dqEntityBuilder;
	@Inject
	DQRestrictionFunctionRegistry dqRestrictionFunctionRegistry;


	/**
	 * For the given constraints and base type generate the dynamic query which is backed by a Criteria object.
	 *
	 * @param constraints
	 * @param clazz
	 * @param baseCriteria
	 *
	 * @return
	 */
	public DQuery buildQueryForUriQuery(final ResultSetConstraint constraints, final Class<?> clazz, final Criteria baseCriteria)
	{
		List<RestrictionFunction> functions = dqRestrictionFunctionRegistry.getFunctions();
		DQuery query = createQuery(clazz, baseCriteria);
		DQCriteriaConstraintDTOMap dqCriteriaConstraintDTOMap = new DQCriteriaConstraintDTOMap();

		//Build up the Query param to Criteria DTO Map
		for (String key : constraints.getConstraints().keySet())
		{
			for (String queryParamValue : constraints.getConstraints().get(key))
			{
				for (RestrictionFunction function : functions)
				{
					function.addRestriction(key, queryParamValue, dqCriteriaConstraintDTOMap);
				}
			}
		}

		//modify query from the built up Map
		for (RestrictionFunction function : functions)
		{
			function.executeChanges(query, dqCriteriaConstraintDTOMap);
		}

		Criteria criteria = query.getCriteria();

		if (!constraints.getOrderings().isEmpty())
		{
			for (Order order : constraints.getOrderings())
				criteria.addOrder(order);
		}
		else
		{
			criteria.addOrder(Order.asc("id")); // apply the default sorting
		}

		// Set Pagination details
		criteria.setFirstResult(constraints.getOffset());
		criteria.setFetchSize(constraints.getLimit());
		criteria.setMaxResults(constraints.getLimit());
		return query;
	}


	/**
	 * create a dynamic query object for the specified Entity class
	 *
	 * @param clazz
	 * @param baseCriteria
	 * 		the base criteria to add restrictions and joins to (optional)
	 *
	 * @return
	 */
	public DQuery createQuery(final Class<?> clazz, Criteria baseCriteria)
	{
		final DQEntity entity = dqEntityBuilder.createEntity(clazz);

		if (baseCriteria != null)
			return new DQuery(entity, baseCriteria);
		else
			return new DQuery(entity, entity.createCriteria());
	}
}
