package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.BaseSessionEventListener;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionEventListener;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Provides transaction helper methods
 */
@Singleton
public class TransactionHelper
{
	private static final Logger log = Logger.getLogger(TransactionHelper.class);

	@Inject
	Provider<Transaction> transactionProvider;

	@Inject
	Provider<Session> sessionProvider;


	/**
	 * get the current hibernate transaction
	 *
	 * @return
	 */
	public final Transaction get()
	{
		return transactionProvider.get();
	}


	/**
	 * Starts a new Hibernate transaction. Note that the caller accepts responsibility for closing the transaction
	 *
	 * @return
	 */
	public HibernateTransaction start()
	{
		final Session session = sessionProvider.get();
		final Transaction tx = session.beginTransaction();

		return new HibernateTransaction(tx);
	}


	/**
	 * Execute the provided {@link Callable} within a transaction, committing if no exceptions are thrown and returning the result
	 * of the Callable
	 *
	 * @param statements
	 * @param <T>
	 *
	 * @return
	 *
	 * @throws Exception
	 */
	public <T> T execute(Callable<T> statements) throws Exception
	{
		try (HibernateTransaction tx = start().withAutoRollback())
		{
			final T ret = statements.call();

			// Success, perform a TX commit
			tx.commit();

			return ret;
		}
	}


	/**
	 * Execute the provided Runnable within a transaction, committing if no exceptions are thrown
	 *
	 * @param statements
	 */
	public void execute(Runnable statements)
	{
		try (HibernateTransaction tx = start().withAutoRollback())
		{
			statements.run();

			// Success, perform a TX commit
			tx.commit();
		}
	}


	void addAction(SessionEventListener... listeners) throws HibernateException
	{
		final Session session = sessionProvider.get();

		if (session.getTransaction().getStatus() != TransactionStatus.ACTIVE)
			throw new IllegalStateException("Cannot add transaction action with no active transaction!");

		session.addEventListeners(listeners);
	}


	/**
	 * Register an action to run on the successful commit of the transaction
	 *
	 * @param action
	 *
	 * @throws HibernateException
	 */
	public void addCommitAction(final Runnable action) throws HibernateException
	{
		if (action == null)
			return; // ignore null actions

		addAction(new BaseSessionEventListener()
		{
			@Override
			public void transactionCompletion(final boolean successful)
			{
				if (successful)
					action.run();
			}
		});
	}


	/**
	 * Register an action to run on the rollback of the transaction
	 *
	 * @param action
	 *
	 * @throws HibernateException
	 */
	public void addRollbackAction(final Runnable action) throws HibernateException
	{
		if (action == null)
			return; // ignore null actions

		addAction(new BaseSessionEventListener()
		{
			@Override
			public void transactionCompletion(final boolean successful)
			{
				if (!successful)
					action.run();
			}
		});
	}


	/**
	 * Adds an action to the transaction to delete a set of files once rollback completes
	 *
	 * @param files
	 */
	public void deleteOnRollback(final Collection<File> files)
	{
		addRollbackAction(new Runnable()
		{
			@Override
			public void run()
			{
				for (File file : files)
				{
					if (log.isTraceEnabled())
						log.trace("Delete file on transaction rollback: " + file);

					final boolean success = FileUtils.deleteQuietly(file);

					if (!success)
						log.warn("Failed to delete file on transaction rollback: " + file);
				}
			}
		});
	}
}
