package com.peterphi.std.guice.hibernate.dialect;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * A custom PostgreSQL Dialect for Hibernate that uses bytea instead of oid for byte[] Lobs
 */
public class PostgresDialect extends PostgreSQLDialect
{
	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor)
	{
		if (sqlTypeDescriptor.getSqlType() == java.sql.Types.BLOB)
			return BinaryTypeDescriptor.INSTANCE;
		else
			return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
	}
}
