package com.peterphi.usermanager.db.dao;

import com.google.inject.Inject;
import com.peterphi.std.guice.hibernatetest.EmbeddedPostgresTestCase;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = "user-manager-hsqldb-in-memory.properties", packages = {"com.peterphi"})
public class UserManagerLiquibasePostgresTests extends EmbeddedPostgresTestCase
{
	@Inject
	UserDaoImpl dao;


	@Test
	public void testFetch()
	{
		dao.getAll();
	}
}
