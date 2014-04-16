package com.peterphi.std.guice.hibernate.usertype;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.hibernate.usertype.UserVersionType;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;

/**
 * Encodes Joda DateTime values into a BIGINT column where the date is expressed in milliseconds since 1970.
 */
public class JodaDateTimeUserType implements UserType, UserVersionType, Comparator
{
	public static JodaDateTimeUserType INSTANCE = new JodaDateTimeUserType();

	private static final int[] SQL_TYPES = {Types.BIGINT};


	@Override
	public int[] sqlTypes()
	{
		return SQL_TYPES;
	}


	@Override
	public Class returnedClass()
	{
		return DateTime.class;
	}


	@Override
	public boolean equals(Object x, Object y) throws HibernateException
	{
		if (x == y)
		{
			return true;
		}
		else if (x == null || y == null)
		{
			return false;
		}
		else
		{
			return x.equals(y);
		}
	}


	@Override
	public int hashCode(final Object x) throws HibernateException
	{
		return x.hashCode();
	}


	@Override
	public DateTime nullSafeGet(final ResultSet resultSet,
	                            final String[] names,
	                            final SessionImplementor session,
	                            final Object owner) throws HibernateException, SQLException
	{
		final long encoded = resultSet.getLong(names[0]);

		if (resultSet.wasNull())
			return null;
		else
		{
			return new DateTime(encoded);
		}
	}


	@Override
	public void nullSafeSet(final PreparedStatement statement,
	                        final Object value,
	                        final int index,
	                        final SessionImplementor session) throws HibernateException, SQLException
	{
		if (value == null)
		{
			statement.setNull(index, Types.BIGINT);
		}
		else
		{
			final long millis = ((DateTime) value).getMillis();

			statement.setLong(index, millis);
		}
	}


	@Override
	public DateTime deepCopy(Object value) throws HibernateException
	{
		return (DateTime) value; // immutable type
	}


	@Override
	public boolean isMutable()
	{
		return false;
	}


	@Override
	public DateTime disassemble(final Object value) throws HibernateException
	{
		if (value == null)
			return null;
		else
			return (DateTime) value;
	}


	@Override
	public DateTime assemble(final Serializable cached, final Object owner) throws HibernateException
	{
		if (cached == null)
			return null;
		else
			return (DateTime) cached;
	}


	@Override
	public DateTime replace(final Object original, final Object target, final Object owner) throws HibernateException
	{
		return (DateTime) original;
	}


	@Override
	public DateTime seed(final SessionImplementor session)
	{
		return DateTime.now();
	}


	@Override
	public DateTime next(final Object current, final SessionImplementor session)
	{
		return seed(session);
	}


	@Override
	public int compare(final Object a, final Object b)
	{
		return ((DateTime) a).compareTo((DateTime) b);
	}
}
