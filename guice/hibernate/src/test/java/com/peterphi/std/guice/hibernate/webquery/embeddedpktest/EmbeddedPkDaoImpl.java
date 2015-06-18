package com.peterphi.std.guice.hibernate.webquery.embeddedpktest;

import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.List;

class EmbeddedPkDaoImpl extends HibernateDao<EmbeddedPkEntity, SomePrimaryKey>
{
	public List<EmbeddedPkEntity> findByTimestamp(final long timestamp)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq("id.timestamp", timestamp));

		return getList(criteria);
	}
}
