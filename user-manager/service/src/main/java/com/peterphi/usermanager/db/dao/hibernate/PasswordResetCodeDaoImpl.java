package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.usermanager.db.entity.PasswordResetEntity;

@Singleton
public class PasswordResetCodeDaoImpl extends HibernateDao<PasswordResetEntity, String>
{
	@Transactional
	public void deleteExpired()
	{
		for (PasswordResetEntity entity : getList(new WebQuery().lt("expires", "now").limit(0)))
			delete(entity);
	}
}
