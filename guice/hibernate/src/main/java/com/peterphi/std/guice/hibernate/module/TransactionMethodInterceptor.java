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
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
		final Session session = sessionProvider.get();

		Timer.Context callTimer = calls.time();

		try
		{
			if (session.getTransaction().isActive())
			{
				// allow silent joining of enclosing transactional methods (NOTE: this ignores the current method's txn-al settings)
				if (log.isTraceEnabled())
					log.trace("Joining existing transaction to call " + invocation.getMethod().toGenericString());

				return invocation.proceed();
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("Creating new transaction to call " + invocation.getMethod().toGenericString());

				final Transactional annotation = readAnnotation(invocation);
				final boolean readOnly = annotation.readOnly();

				// We are responsible for creating+closing the connection
				Timer.Context ownerTimer = transactionStartedCalls.time();
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
		}
		finally
		{
			callTimer.stop();
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
	}


	/**
	 * Make the session (and the underlying {@link java.sql.Connection} read only
	 *
	 * @param session
	 */
	private void makeReadOnly(final Session session)
	{
		session.setDefaultReadOnly(true);
		session.setFlushMode(FlushMode.MANUAL);

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
		return isInstanceOf(e, annotation.rollbackOn()) && !isInstanceOf(e, annotation.exceptOn());
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
