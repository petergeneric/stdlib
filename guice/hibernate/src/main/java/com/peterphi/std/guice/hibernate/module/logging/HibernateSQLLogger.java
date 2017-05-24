package com.peterphi.std.guice.hibernate.module.logging;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Uses a Hibernate {@link org.hibernate.Interceptor} to intercept SQL Statement preparations as they happen on the local thread
 */
public class HibernateSQLLogger implements Closeable, AutoCloseable, Consumer<String>
{
	private static final Logger log = Logger.getLogger(HibernateSQLLogger.class);

	private final List<String> statements = new ArrayList<>(0);
	private final HibernateObservingInterceptor interceptor;


	public HibernateSQLLogger(HibernateObservingInterceptor interceptor)
	{
		this.interceptor = interceptor;
	}


	public void start()
	{
		interceptor.addThreadLocalSQLAuditor(this);
	}


	@Override
	public void accept(final String sql)
	{
		if (log.isTraceEnabled())
			log.trace("Hibernate executing SQL: " + sql);

		synchronized (statements)
		{
			statements.add(sql);
		}
	}


	@Override
	public void close()
	{
		interceptor.removeThreadLocalSQLAuditor(this);
	}


	/**
	 * Return a read-only copy of the statements prepared since this logger was started and before the logger was stopped; if the
	 * logger has not yet been stopped then it will return the statements prepared thus far
	 *
	 * @return
	 */
	public List<String> getAllStatements()
	{
		synchronized (statements)
		{
			return Collections.unmodifiableList(new ArrayList<>(statements));
		}
	}
}
