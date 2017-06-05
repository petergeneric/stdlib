package com.peterphi.std.guice.liquibase;

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
	UPDATE,
	/**
	 * Mark that all pending changesets have been applied without actually executing any of their logic (this is "changeLogSync"
	 * at the liquibase command-line)
	 */
	MARK_UPDATED,

	/**
	 * Generate a changelog XML and dump it to stdout
	 */
	GENERATE_CHANGELOG;


	public boolean isWriteAction()
	{
		return (this == UPDATE) || (this == MARK_UPDATED);
	}
}
