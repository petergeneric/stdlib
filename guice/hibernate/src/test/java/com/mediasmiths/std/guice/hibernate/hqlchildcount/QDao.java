package com.mediasmiths.std.guice.hibernate.hqlchildcount;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;

import java.util.List;

public class QDao extends HibernateDao<QEntity, Long>
{
	@Transactional
	public List<Long> getIdsByQuery(String query)
	{
		return getIdList(createQuery(query));
	}
}
