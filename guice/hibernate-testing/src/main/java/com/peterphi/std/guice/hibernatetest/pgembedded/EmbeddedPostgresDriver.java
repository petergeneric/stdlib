package com.peterphi.std.guice.hibernatetest.pgembedded;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class EmbeddedPostgresDriver implements Driver
{
	private static final Map<String, EmbeddedPostgres> INSTANCES = new HashMap<>();

	static
	{
		try
		{
			DriverManager.registerDriver(new EmbeddedPostgresDriver());
		}
		catch (Throwable t)
		{
			System.out.println("Error registering SQL Driver: " + t.getMessage());
			t.printStackTrace(System.out);
		}
	}

	public EmbeddedPostgresDriver()
	{
	}


	@Override
	public Connection connect(final String url, final Properties info) throws SQLException
	{
		if (!acceptsURL(url))
			return null;

		synchronized (INSTANCES)
		{
			if (!INSTANCES.containsKey(url))
			{
				try
				{
					EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();

					final EmbeddedPostgres instance = builder.start();
					INSTANCES.put(url, instance);
				}
				catch (Throwable t)
				{
					throw new SQLException("Error building EmbeddedPostgres: " + t.getMessage(), t);
				}
			}

			final DataSource db = INSTANCES.get(url).getPostgresDatabase();
			final Connection conn = db.getConnection();

			// TODO track this Connection until it's closed and shut down the database instance once all closed?

			return conn;
		}
	}


	@Override
	public boolean acceptsURL(final String url) throws SQLException
	{
		return url.startsWith("jdbc:embedded-postgres:");
	}


	@Override
	public DriverPropertyInfo[] getPropertyInfo(final String url, Properties info) throws SQLException
	{
		if (!acceptsURL(url))
		{
			return new DriverPropertyInfo[0];
		}

		if (info == null)
			info = new Properties();

		String[] choices = new String[]{"true", "false"};
		DriverPropertyInfo[] pinfo = new DriverPropertyInfo[6];
		DriverPropertyInfo p;


		p = new DriverPropertyInfo("user", null);
		p.value = info.getProperty("user");
		p.required = true;
		pinfo[0] = p;
		p = new DriverPropertyInfo("password", null);
		p.value = info.getProperty("password");
		p.required = true;
		pinfo[1] = p;
		p = new DriverPropertyInfo("get_column_name", null);
		p.value = info.getProperty("get_column_name", "true");
		p.required = false;
		p.choices = choices;
		pinfo[2] = p;
		p = new DriverPropertyInfo("ifexists", null);
		p.value = info.getProperty("ifexists", "false");
		p.required = false;
		p.choices = choices;
		pinfo[3] = p;
		p = new DriverPropertyInfo("default_schema", null);
		p.value = info.getProperty("default_schema", "false");
		p.required = false;
		p.choices = choices;
		pinfo[4] = p;
		p = new DriverPropertyInfo("shutdown", null);
		p.value = info.getProperty("shutdown", "false");
		p.required = false;
		p.choices = choices;
		pinfo[5] = p;

		return pinfo;
	}


	@Override
	public int getMajorVersion()
	{
		return 1;
	}


	@Override
	public int getMinorVersion()
	{
		return 0;
	}


	@Override
	public boolean jdbcCompliant()
	{
		return true;
	}


	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		throw new SQLFeatureNotSupportedException("not yet supported");
	}
}
