package com.peterphi.servicemanager.service.db;

import com.google.inject.Inject;
import com.peterphi.servicemanager.service.db.dao.impl.LetsEncryptCertificateDaoImpl;
import com.peterphi.servicemanager.service.db.dao.impl.ServiceInstanceDaoImpl;
import com.peterphi.servicemanager.service.db.entity.LetsEncryptAccountEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceInstanceEntity;
import com.peterphi.servicemanager.service.db.entity.ResourceTemplateEntity;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "service-manager-hsqldb-in-memory.properties", packages = {"com.peterphi"})
public class ServiceManagerDatabaseTest
{
	@Inject
	HibernateDao<ResourceTemplateEntity, String> templateDao;
	@Inject
	HibernateDao<ResourceInstanceEntity, String> instanceDao;

	@Inject
	ServiceInstanceDaoImpl serviceInstanceDao;

	@Inject
	LetsEncryptCertificateDaoImpl certDao;

	@Inject
	HibernateDao<LetsEncryptAccountEntity,Integer> accountDao;

	@Test
	public void doTest()
	{
		templateDao.getAll();
		instanceDao.getAll();
		serviceInstanceDao.getAll();


		certDao.getAll();
		accountDao.getAll();
	}
}
