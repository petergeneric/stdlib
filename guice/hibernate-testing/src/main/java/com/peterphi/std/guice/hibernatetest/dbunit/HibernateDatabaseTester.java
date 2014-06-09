package com.peterphi.std.guice.hibernatetest.dbunit;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.dbunit.AbstractDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateDatabaseTester extends AbstractDatabaseTester
{
	@Inject
	ServiceRegistry registry;

	@Inject
	Configuration configuration;

	@Inject(optional = true)
	@Named("dbunit.schema")
	String schema = null;


	@Override
	public IDatabaseConnection getConnection()
	{
		return new HibernateDatabaseConnection(registry, schema);
	}
}
