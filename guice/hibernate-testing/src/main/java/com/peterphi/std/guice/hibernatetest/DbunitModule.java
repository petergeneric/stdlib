package com.peterphi.std.guice.hibernatetest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.peterphi.std.guice.hibernatetest.dbunit.HibernateDatabaseTester;
import com.peterphi.std.util.ListUtility;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.QueryDataSet;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Table;

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
	public QueryDataSet getDatabaseDataSet(HibernateDatabaseTester tester,
	                                       Configuration config) throws AmbiguousTableNameException
	{

		final QueryDataSet dataset = new QueryDataSet(tester.getConnection());

		for (Table table : ListUtility.iterate(config.getTableMappings()))
		{
			dataset.addTable(table.getName());
		}

		return dataset;
	}
}
