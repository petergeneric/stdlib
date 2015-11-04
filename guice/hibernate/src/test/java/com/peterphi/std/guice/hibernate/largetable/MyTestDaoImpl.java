package com.peterphi.std.guice.hibernate.largetable;

import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.DetachedCriteriaHelper;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

 class MyTestDaoImpl extends HibernateDao<LargeTableSimplePKEntity, Long>
{
	/**
	 * Run
	 * @return
	 */
	@Transactional(readOnly = true)
	public long countWithAInName()
	{
		Criteria criteria = createCriteria();

		criteria.add(Subqueries.propertyIn("id",
		                                   DetachedCriteriaHelper.wrap(createCriteria().setProjection(Projections.id())
		                                                                               .add(Restrictions.like("name",
		                                                                                                          "%a%")))));

		return criteria.list().size();
	}
}
