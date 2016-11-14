package com.peterphi.servicemanager.service.db.dao.impl;

import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;


@Singleton
public class ResourceTemplateDaoImpl extends HibernateDao<ResourceTemplateEntity, String>
{
}
