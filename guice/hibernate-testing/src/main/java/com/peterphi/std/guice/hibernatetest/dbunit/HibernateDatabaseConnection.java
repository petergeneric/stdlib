package com.peterphi.std.guice.hibernatetest.dbunit;

import org.dbunit.database.AbstractDatabaseConnection;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class HibernateDatabaseConnection extends AbstractDatabaseConnection
{
	private final ConnectionProvider connectionProvider;
	private final String schema;


	private Connection connection = null;


	public HibernateDatabaseConnection(final ServiceRegistry registry, String schema)
	{
		this.connectionProvider = registry.getService(ConnectionProvider.class);
		this.schema = schema;
	}


	@Override
	public synchronized Connection getConnection() throws SQLException
	{
		if (this.connection == null)
		{
			this.connection = this.connectionProvider.getConnection();
		}

		return this.connection;
	}


	@Override
	public String getSchema()
	{
		return schema;
	}


	@Override
	public synchronized void close() throws SQLException
	{
		if (this.connection != null)
		{
			connectionProvider.closeConnection(this.connection);
			this.connection = null;
		}
	}
}
