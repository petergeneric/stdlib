package com.peterphi.std.guice.hibernate.module.logging;

import com.google.inject.Singleton;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Uses hibernate's {@link Interceptor} class behind the scenes to allow observers to hook into events (N.B. but not alter them)
 */
@Singleton
public class HibernateObservingInterceptor
{
	private HibernateInterceptorImpl instance;

	private ThreadLocal<List<Consumer<String>>> onPrepareStatementWatchers = new ThreadLocal<>();


	public HibernateObservingInterceptor()
	{
		instance = new HibernateInterceptorImpl(this);
	}


	public Interceptor getInterceptor()
	{
		return instance;
	}


	private void onPrepareStatement(String sql)
	{
		final List<Consumer<String>> consumers = onPrepareStatementWatchers.get();

		if (consumers != null)
		{
			for (Consumer<String> consumer : consumers)
			{
				consumer.accept(sql);
			}
		}
	}


	/**
	 * Start a new logger which records SQL Prepared Statements created by Hibernate in this Thread. Must be closed (or treated as
	 * autoclose)
	 *
	 * @return
	 */
	public HibernateSQLLogger startSQLLogger()
	{
		final HibernateSQLLogger logger = new HibernateSQLLogger(this);

		logger.start();

		return logger;
	}


	/**
	 * Add an observer to <code>onPrepareStatement</code> calls made by the current Thread until {@link
	 * #clearThreadLocalObservers()} is
	 * called
	 *
	 * @param observer
	 */
	public void addThreadLocalSQLAuditor(Consumer<String> observer)
	{
		List<Consumer<String>> observers = onPrepareStatementWatchers.get();

		if (observers == null)
		{
			observers = new CopyOnWriteArrayList<>();
			onPrepareStatementWatchers.set(observers);
		}

		observers.add(observer);
	}


	/**
	 * Remove a previously added observer for <code>onPrepareStatement</code> calls
	 *
	 * @param observer
	 */
	public void removeThreadLocalSQLAuditor(Consumer<String> observer)
	{
		List<Consumer<String>> observers = onPrepareStatementWatchers.get();

		observers.remove(observer);

		if (observers.isEmpty())
			onPrepareStatementWatchers.remove();
	}


	/**
	 * Clear any Thread Local observers
	 */
	public void clearThreadLocalObservers()
	{
		onPrepareStatementWatchers.remove();
	}


	/**
	 * The hibernate interceptor; extends EmptyInterceptor because we are only observing
	 */
	private static class HibernateInterceptorImpl extends EmptyInterceptor
	{
		private final HibernateObservingInterceptor parent;


		public HibernateInterceptorImpl(final HibernateObservingInterceptor parent)
		{
			this.parent = parent;
		}


		@Override
		public String onPrepareStatement(String sql)
		{
			// Notify the observers
			parent.onPrepareStatement(sql);

			// Return unmodified
			return sql;
		}
	}
}
