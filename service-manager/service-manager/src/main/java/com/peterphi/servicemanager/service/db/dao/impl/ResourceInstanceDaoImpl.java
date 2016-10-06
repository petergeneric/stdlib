package com.peterphi.servicemanager.service.db.dao.impl;

import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.rest.resource.type.ResourceInstanceState;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;


@Singleton
public class ResourceInstanceDaoImpl extends HibernateDao<ResourceInstanceEntity, Integer>
{
	@Transactional(readOnly = true)
	public List<Integer> getByProviderAndState(final String provider, ResourceInstanceState... states)
	{
		final Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("provider", provider));
		criteria.add(Restrictions.in("state", states));

		criteria.addOrder(Order.asc("updated"));

		criteria.setProjection(Projections.id());

		return getIdList(criteria);
	}
}
