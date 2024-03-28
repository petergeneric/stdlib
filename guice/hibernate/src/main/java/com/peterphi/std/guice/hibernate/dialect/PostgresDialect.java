package com.peterphi.std.guice.hibernate.dialect;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;

import static org.hibernate.type.SqlTypes.BLOB;

/**
 * A custom PostgreSQL Dialect for Hibernate that uses bytea instead of oid for byte[] Lobs
 */
public class PostgresDialect extends PostgreSQLDialect
{
	public PostgresDialect()
	{
		super(DatabaseVersion.make(15, 4));
	}


	protected String columnType(int sqlTypeCode)
	{
		switch (sqlTypeCode)
		{
			case BLOB:
				return "bytea";
			default:
				return super.columnType(sqlTypeCode);
		}
	}
}
