package com.peterphi.servicemanager.service.db.dao.impl;

import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ServiceInstanceEntity;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;

@Singleton
public class ServiceInstanceDaoImpl extends HibernateDao<ServiceInstanceEntity, String>
{
}
