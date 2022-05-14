package com.peterphi.std.guice.liquibase.hibernate;

import liquibase.database.core.MSSQLDatabase;
import liquibase.structure.core.Schema;

/**
 * Extension to liquibase MSSQLDatabase that bypasses the prohibition on changing the default schema name
 */
public final class DefaultSchemaPermittingMSSQLDatabase extends MSSQLDatabase
{
	public DefaultSchemaPermittingMSSQLDatabase()
	{
		super();
	}


	@Override
	public void setDefaultSchemaName(final String schemaName)
	{
		this.setOutputDefaultSchema(true);
		this.defaultSchemaName = correctObjectName(schemaName, Schema.class);
	}


	@Override
	public int getPriority()
	{
		return 999;
	}
}
