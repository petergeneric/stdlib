package com.peterphi.usermanager.db.dao;

import com.peterphi.usermanager.db.dao.hibernate.RoleDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.google.inject.Inject;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "user-manager-hsqldb-in-memory.properties", packages = {"com.peterphi"})
public class CreateDatabaseSchemaTest
{
	@Inject
	UserDaoImpl userDao;

	@Inject
	RoleDaoImpl roleDao;


	@Test
	public void testDb()
	{
		userDao.getAll();
		roleDao.getAll();
	}
}
