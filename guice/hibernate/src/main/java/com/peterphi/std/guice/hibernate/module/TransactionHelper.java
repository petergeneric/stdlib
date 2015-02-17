package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.io.File;
import java.util.Collection;

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


	public void addAction(Synchronization synchronisation) throws HibernateException
	{
		if (synchronisation == null)
			return; // ignore null actions

		final Transaction tx = get();

		if (!tx.isActive())
			throw new IllegalStateException("Cannot add transaction action with no active transaction!");

		tx.registerSynchronization(synchronisation);
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

		addAction(new Synchronization()
		{
			@Override
			public void beforeCompletion()
			{
				// no action required
			}


			@Override
			public void afterCompletion(final int status)
			{
				if (status == Status.STATUS_COMMITTED)
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

		addAction(new Synchronization()
		{
			@Override
			public void beforeCompletion()
			{
				// no action required
			}


			@Override
			public void afterCompletion(final int status)
			{
				if (status == Status.STATUS_ROLLEDBACK)
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
