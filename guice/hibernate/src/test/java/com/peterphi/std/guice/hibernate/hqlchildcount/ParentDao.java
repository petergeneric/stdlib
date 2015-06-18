package com.peterphi.std.guice.hibernate.hqlchildcount;

import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;

import java.util.List;

public class ParentDao extends HibernateDao<ParentEntity, Long>
{
	@Transactional
	public List<Long> getIdsByQuery(String query)
	{
		return getIdList(createQuery(query));
	}
}
