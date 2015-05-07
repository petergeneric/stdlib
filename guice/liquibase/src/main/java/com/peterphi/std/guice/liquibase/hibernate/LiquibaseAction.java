package com.peterphi.std.guice.liquibase.hibernate;

public enum LiquibaseAction
{
	/**
	 * Disable liquibase
	 */
	IGNORE,
	/**
	 * Assert that there are no pending changesets
	 */
	ASSERT_UPDATED,
	/**
	 * Update all changesets
	 */
	UPDATE;


	public boolean isWriteAction()
	{
		return (this == UPDATE);
	}
}
