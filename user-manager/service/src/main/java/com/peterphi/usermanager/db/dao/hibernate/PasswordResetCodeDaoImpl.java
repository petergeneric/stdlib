package com.peterphi.usermanager.db.dao.hibernate;

import com.google.inject.Singleton;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.usermanager.db.entity.PasswordResetEntity;

@Singleton
public class PasswordResetCodeDaoImpl extends HibernateDao<PasswordResetEntity, String>
{
}
