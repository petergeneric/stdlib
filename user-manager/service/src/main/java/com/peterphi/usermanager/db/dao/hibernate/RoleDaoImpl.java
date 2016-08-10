package com.peterphi.usermanager.db.dao.hibernate;

import com.peterphi.usermanager.db.entity.RoleEntity;
import com.google.inject.Singleton;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;

@Singleton
public class RoleDaoImpl extends HibernateDao<RoleEntity, String>
{
}
