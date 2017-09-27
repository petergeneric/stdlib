package com.peterphi.std.guice.hibernatetest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.peterphi.std.guice.hibernatetest.dbunit.HibernateDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.dataset.IDataSet;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.SQLException;

public class DbunitModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(IDatabaseTester.class).to(HibernateDatabaseTester.class);
	}


	/**
	 * Produces a QueryDataSet for the database contents
	 *
	 * @param tester
	 * @param config
	 *
	 * @return
	 *
	 * @throws AmbiguousTableNameException
	 */
	@Provides
	public IDataSet getDatabaseDataSet(HibernateDatabaseTester tester,
	                                   SessionFactory fac,
	                                   Configuration config) throws AmbiguousTableNameException
	{
		try
		{
			return tester.getConnection().createDataSet();
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Failed to create dataset", e);
		}
	}
}
