package com.peterphi.std.guice.hibernate.module;

import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Hibernate ReturningWork implementation that sets the readOnly flag on the connection, returning whether it was changed
 */
final class SetJDBCConnectionReadOnlyWork implements Work
{
	/**
	 * Sets the Connection to read-only, returning true if it was changed (i.e. if it was previously read-write)
	 */
	public static final Work READ_ONLY = new SetJDBCConnectionReadOnlyWork(true);
	/**
	 * Sets the Connection to read-write, returning true if it was changed (i.e. if it was previously read-only)
	 */
	public static final Work READ_WRITE = new SetJDBCConnectionReadOnlyWork(false);

	private final boolean value;

	private SetJDBCConnectionReadOnlyWork(boolean value)
	{
		this.value = value;
	}

	@Override
	public void execute(final Connection connection) throws SQLException
	{
		connection.setReadOnly(value);
	}
}
