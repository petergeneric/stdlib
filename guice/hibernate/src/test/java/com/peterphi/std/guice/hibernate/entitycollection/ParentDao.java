package com.peterphi.std.guice.hibernate.entitycollection;

import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;

import java.util.List;

public class ParentDao extends HibernateDao<ParentEntity, Long>
{
	@Transactional(readOnly = true)
	public List<Long> getIdsByQuery(String query)
	{
		return getIdList(createQuery(query));
	}


	@Transactional(readOnly = true)
	public List<ParentEntity> getByQuery(String query)
	{
		return (List<ParentEntity>) getList(createQuery(query));
	}
}
