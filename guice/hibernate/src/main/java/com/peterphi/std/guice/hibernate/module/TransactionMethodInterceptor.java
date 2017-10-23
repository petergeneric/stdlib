package com.peterphi.std.guice.hibernate.module;

/*
 * Based on warp-persist HibernateLocalTxnInterceptor which is originally
 * 
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.metrics.GuiceMetricNames;
import com.peterphi.std.guice.database.annotation.Transactional;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.lang.reflect.Method;

/**
 * A Guice AOP interceptor for methods annotated with the {@link Transactional} annotation to transparently start and
 * commit/rollback a Hibernate transaction
 */
class TransactionMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(TransactionMethodInterceptor.class);

	private final Provider<Session> sessionProvider;

	private final Timer calls;
	private final Timer transactionStartedCalls;
	private final Meter errorRollbacks;
	private final Meter commitFailures;


	public TransactionMethodInterceptor(Provider<Session> sessionProvider, MetricRegistry metrics)
	{
		this.sessionProvider = sessionProvider;

		this.calls = metrics.timer(GuiceMetricNames.TRANSACTION_CALLS_TIMER);
		this.transactionStartedCalls = metrics.timer(GuiceMetricNames.TRANSACTION_OWNER_CALLS_TIMER);
		this.errorRollbacks = metrics.meter(GuiceMetricNames.TRANSACTION_ERROR_ROLLBACK_METER);
		this.commitFailures = metrics.meter(GuiceMetricNames.TRANSACTION_COMMIT_FAILURE_METER);
	}


	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable
	{
		Timer.Context callTimer = calls.time();

		try
		{
			if (sessionProvider.get().getTransaction().getStatus() == TransactionStatus.ACTIVE)
			{
				// allow silent joining of enclosing transactional methods (NOTE: this ignores the current method's txn-al settings)
				if (log.isTraceEnabled())
					log.trace("Joining existing transaction to call " + invocation.getMethod().toGenericString());

				return invocation.proceed();
			}
			else
			{
				final Transactional annotation = readAnnotation(invocation);

				// If we are allowed to auto-retry, first run the method with exception-catching retry behaviour
				// After the max attempts for auto retry are exhausted we'll fall back on the non-retrying default behaviour
				if (annotation.autoRetry())
				{
					// Try all but the last attempt
					final int retries = Math.max(0, annotation.autoRetryCount() - 1);

					// N.B. more aggressive than the @Retry annotation implements
					long backoff = 1000;
					final double multiplier = 1.5;

					for (int attempt = 0; attempt < retries; attempt++)
					{
						try
						{
							return createTransactionAndExecuteMethod(invocation, annotation);
						}
						catch (LockAcquisitionException | StaleStateException | GenericJDBCException | OptimisticLockException e)
						{
							log.warn("@Transactional caught exception " + e.getClass().getSimpleName() + "; retrying...", e);

							try
							{
								Thread.sleep(backoff);
							}
							catch (InterruptedException ie)
							{
								throw new RuntimeException("Interrupted while attempting a @Transactional retry!", ie);
							}

							// Increase backoff for the next exception
							backoff *= multiplier;
						}
					}
				}

				// Run without further retries
				return createTransactionAndExecuteMethod(invocation, annotation);
			}
		}
		finally
		{
			callTimer.stop();
		}
	}


	private Object createTransactionAndExecuteMethod(final MethodInvocation invocation,
	                                                 final Transactional annotation) throws Throwable
	{
		if (log.isTraceEnabled())
			log.trace("Creating new transaction to call " + invocation.getMethod().toGenericString());


		final boolean readOnly = annotation.readOnly();

		// We are responsible for creating+closing the connection
		Timer.Context ownerTimer = transactionStartedCalls.time();

		final Session session = sessionProvider.get();
		try
		{
			// no transaction already started, so start one and enforce its semantics
			final Transaction tx = session.beginTransaction();

			if (readOnly)
				makeReadOnly(session);
			else
				makeReadWrite(session);

			// Execute the method
			final Object result;
			try
			{
				result = invocation.proceed();
			}
			catch (Exception e)
			{
				if (shouldRollback(annotation, e))
				{
					errorRollbacks.mark();

					rollback(tx, e);
				}
				else
				{
					complete(tx, readOnly);
				}

				// propagate the exception
				throw e;
			}
			catch (Error e)
			{
				errorRollbacks.mark();

				rollback(tx);

				// propagate the error
				throw e;
			}

			// The method completed successfully, we can complete the the transaction
			// we can't move into the above try block because it'll interfere with the do not move into try block as it interferes with the advised method's throwing semantics
			RuntimeException commitException = null;
			try
			{
				complete(tx, readOnly);
			}
			catch (RuntimeException e)
			{
				commitFailures.mark();

				rollback(tx);

				commitException = e;
			}

			// propagate anyway
			if (commitException != null)
				throw commitException;

			// or return result
			return result;
		}
		finally
		{
			ownerTimer.stop();

			if (session.isOpen())
			{
				// Close the session
				session.close();
			}
		}
	}


	/**
	 * Make the {@link java.sql.Connection} underlying this {@link Session} read/write
	 *
	 * @param session
	 */
	private void makeReadWrite(final Session session)
	{
		session.doWork(SetJDBCConnectionReadOnlyWork.READ_WRITE);
		session.setHibernateFlushMode(FlushMode.AUTO);
	}


	/**
	 * Make the session (and the underlying {@link java.sql.Connection} read only
	 *
	 * @param session
	 */
	private void makeReadOnly(final Session session)
	{
		session.setDefaultReadOnly(true);
		session.setHibernateFlushMode(FlushMode.MANUAL);

		// Make the Connection read only
		session.doWork(SetJDBCConnectionReadOnlyWork.READ_ONLY);
	}


	/**
	 * Read the Transactional annotation for a given method invocation
	 *
	 * @param invocation
	 *
	 * @return
	 */
	private Transactional readAnnotation(MethodInvocation invocation)
	{
		final Method method = invocation.getMethod();

		if (method.isAnnotationPresent(Transactional.class))
		{
			return method.getAnnotation(Transactional.class);
		}
		else
		{
			throw new RuntimeException("Could not find Transactional annotation");
		}
	}


	/**
	 * @param annotation
	 * 		The metadata annotation of the method
	 * @param e
	 * 		The exception to test for rollback
	 *
	 * @return returns true if the transaction should be rolled back, otherwise false
	 */
	private boolean shouldRollback(Transactional annotation, Exception e)
	{
		return isInstanceOf(e, annotation.rollbackOn()) && !isInstanceOf(e, annotation.exceptOn()) ||
		       isSqlServerSnapshotConflictError(e);
	}


	/**
	 * Special-case for SQL Server SNAPSHOT isolation level: transaction conflicts result in an exception that hibernate is not properly aware of and cannot catch.<br />
	 * It looks like this:
	 * <pre>javax.persistence.PersistenceException: org.hibernate.exception.SQLGrammarException: could not execute statement
	 at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:149)
	 at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:157)
	 at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:164)
	 at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1443)
	 at org.hibernate.internal.SessionImpl.managedFlush(SessionImpl.java:493)
	 at org.hibernate.internal.SessionImpl.flushBeforeTransactionCompletion(SessionImpl.java:3207)
	 at org.hibernate.internal.SessionImpl.beforeTransactionCompletion(SessionImpl.java:2413)
	 at org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl.beforeTransactionCompletion(JdbcCoordinatorImpl.java:467)
	 at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.beforeCompletionCallback(JdbcResourceLocalTransactionCoordinatorImpl.java:156)
	 at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.access$100(JdbcResourceLocalTransactionCoordinatorImpl.java:38)
	 at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl$TransactionDriverControlImpl.commit(JdbcResourceLocalTransactionCoordinatorImpl.java:231)
	 at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:68)
	 at com.peterphi.std.guice.hibernate.module.TransactionMethodInterceptor.complete(TransactionMethodInterceptor.java:316)
	 at com.peterphi.std.guice.hibernate.module.TransactionMethodInterceptor.createTransactionAndExecuteMethod(TransactionMethodInterceptor.java:196)
	 at com.peterphi.std.guice.hibernate.module.TransactionMethodInterceptor.invoke(TransactionMethodInterceptor.java:102)
	 Caused by: org.hibernate.exception.SQLGrammarException: could not execute statement
	 at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:106)
	 at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:42)
	 at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:111)
	 at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:97)
	 at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:178)
	 at org.hibernate.engine.jdbc.batch.internal.NonBatchingBatch.addToBatch(NonBatchingBatch.java:45)
	 at org.hibernate.persister.entity.AbstractEntityPersister.update(AbstractEntityPersister.java:3198)
	 at org.hibernate.persister.entity.AbstractEntityPersister.updateOrInsert(AbstractEntityPersister.java:3077)
	 at org.hibernate.persister.entity.AbstractEntityPersister.update(AbstractEntityPersister.java:3457)
	 at org.hibernate.action.internal.EntityUpdateAction.execute(EntityUpdateAction.java:145)
	 at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:589)
	 at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:463)
	 at org.hibernate.event.internal.AbstractFlushingEventListener.performExecutions(AbstractFlushingEventListener.java:337)
	 at org.hibernate.event.internal.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:39)
	 at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1437)
	 ... 14 more
	 Caused by: com.microsoft.sqlserver.jdbc.SQLServerException: Snapshot isolation transaction aborted due to update conflict. You cannot use snapshot isolation to access table 'table_name' directly or indirectly in database 'database_name' to update, delete, or insert the row that has been modified or deleted by another transaction. Retry the transaction or change the isolation level for the update/delete statement.
	 at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(SQLServerException.java:217)
	 at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(SQLServerStatement.java:1655)
	 at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(SQLServerPreparedStatement.java:440)
	 at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(SQLServerPreparedStatement.java:385)
	 at com.microsoft.sqlserver.jdbc.TDSCommand.execute(IOBuffer.java:7505)</pre>
	 * @param e
	 * @return
	 */
	private boolean isSqlServerSnapshotConflictError(Throwable e)
	{
		// Looking for PersistenceException, wrapping a SQLGrammarException, wrapping a SQLServerException with text "Snapshot isolation transaction aborted due to update conflict"
		if (e != null && e instanceof PersistenceException)
		{
			e = e.getCause();

			if (e != null && e instanceof SQLGrammarException)
			{
				e = e.getCause();

				if (e != null && StringUtils.equals(e.getClass().getName(), "com.microsoft.sqlserver.jdbc.SQLServerException")) {
					return (StringUtils.startsWith(e.getMessage(), "Snapshot isolation transaction aborted due to update conflict"));
				}
			}
		}

		// Does not appear to match
		return false;
	}


	private static boolean isInstanceOf(Exception e, Class<? extends Exception>[] classes)
	{
		for (Class<? extends Exception> type : classes)
		{
			if (type.isInstance(e))
				return true;
		}

		return false;
	}


	/**
	 * Complete the transaction
	 *
	 * @param tx
	 * @param readOnly
	 * 		the read-only flag on the transaction (if true, the transaction will be rolled back, otherwise the transaction will be
	 * 		committed)
	 */
	private final void complete(Transaction tx, boolean readOnly)
	{
		if (log.isTraceEnabled())
			log.trace("Complete " + tx);

		if (!readOnly)
			tx.commit();
		else
			tx.rollback();
	}


	private final void rollback(Transaction tx)
	{
		if (log.isTraceEnabled())
			log.trace("Rollback " + tx);

		tx.rollback();
	}


	private final void rollback(Transaction tx, Exception e)
	{
		if (log.isDebugEnabled())
			log.debug(e.getClass().getSimpleName() + " causes rollback");

		tx.rollback();
	}
}
