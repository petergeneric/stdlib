package com.peterphi.std.guice.liquibase.hibernate;

import com.google.inject.Inject;
import com.peterphi.std.guice.hibernate.dao.HibernateDao;
import com.peterphi.std.guice.testing.GuiceUnit;
import com.peterphi.std.guice.testing.com.peterphi.std.guice.testing.annotations.GuiceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(GuiceUnit.class)
@GuiceConfig(config = {"liquibase-hibernate-tests-in-memory-hsqldb.properties"}, classPackages = SomeEntity.class)
public class LiquibaseCoreTest
{
	@Inject
	HibernateDao<SomeEntity, Long> dao;


	@Test
	public void testThatLiquibaseMigrationInsertedTestData()
	{
		assertEquals("Initial data insert should have succeeded", 1, dao.getAll().size());
		assertEquals("Initial data insert name field", "Example Object", dao.getById(1L).name);
	}
}
