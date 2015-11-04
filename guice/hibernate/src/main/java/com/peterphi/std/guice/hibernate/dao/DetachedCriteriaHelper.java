package com.peterphi.std.guice.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.internal.CriteriaImpl;

/**
 * Allows a Criteria to be wrapped as a DetachedCriteria, allowing the use of Criteria objects in subqueries without having to
 * follow an entirely separate codepath. Should
 */
public class DetachedCriteriaHelper extends DetachedCriteria
{
	private DetachedCriteriaHelper(final Criteria criteria)
	{
		super((CriteriaImpl) criteria, criteria);
	}


	/**
	 * Wraps an existing Criteria as a DetachedCriteria
	 *
	 * @param criteria
	 *
	 * @return
	 */
	public static DetachedCriteria wrap(Criteria criteria)
	{
		return new DetachedCriteriaHelper(criteria);
	}
}
