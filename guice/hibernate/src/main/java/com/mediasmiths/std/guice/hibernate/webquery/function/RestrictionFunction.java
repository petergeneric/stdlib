package com.mediasmiths.std.guice.hibernate.webquery.function;

import com.mediasmiths.std.guice.hibernate.webquery.DQCriteriaConstraintDTO;
import com.mediasmiths.std.guice.hibernate.webquery.DQCriteriaConstraintDTOMap;
import com.mediasmiths.std.guice.hibernate.webquery.DQuery;
import com.mediasmiths.std.guice.hibernate.webquery.RestrictionFunctionType;

import java.util.HashMap;
import java.util.List;

/**
 * The base class for operator functions that can be applied to a Dynamic Query
 */
public abstract class RestrictionFunction
{
	/**
	 * Should this function perform actions on the given query param value?
	 *
	 * @param queryParamValue
	 *
	 * @return
	 */
	public abstract boolean isApplicable(final String queryParamValue);

	/**
	 * Adds/Modifies the DTO so that the executeChanges method can modify the Dynamic query
	 *
	 * @param field
	 * @param queryParamValue
	 * @param constraints
	 *
	 * @return True/false restriction was added
	 */
	public boolean addRestriction(final String field, final String queryParamValue, final DQCriteriaConstraintDTOMap constraints)
	{
		if (isApplicable(queryParamValue))
		{
			constraints.put(getType(), field, getUserInputValue(queryParamValue));
			return true;
		}

		return false;
	}

	/**
	 * @return The enum function type
	 */
	public abstract RestrictionFunctionType getType();

	public abstract String getUserInputValue(final String queryParamValue);

	/**
	 * The required abstract method that determines how the DQuery should be used.
	 *
	 * @param query
	 * @param key
	 * @param values
	 */
	abstract void modifyQuery(final DQuery query, String key, List<String> values);

	/**
	 * For the given query and Constraints DTO perform the changes to the DQuery
	 *
	 * @param query
	 * @param constraints
	 */
	public void executeChanges(final DQuery query, DQCriteriaConstraintDTOMap constraints)
	{
		HashMap<String, DQCriteriaConstraintDTO> entries = constraints.getEntries(getType());
		if (entries != null)
		{
			for (String fieldKey : entries.keySet())
			{
				modifyQuery(query, fieldKey, entries.get(fieldKey).values);
			}
		}
	}
}
