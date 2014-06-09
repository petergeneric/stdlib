package com.peterphi.std.guice.hibernate.module;

import org.hibernate.Transaction;

import java.util.concurrent.atomic.AtomicBoolean;

public class HibernateTransaction implements AutoCloseable
{
	private final Transaction tx;

	private AtomicBoolean closed = new AtomicBoolean(false);
	private boolean rollback = true;


	public HibernateTransaction(final Transaction tx)
	{
		this.tx = tx;
	}


	/**
	 * Set the automatic close action to rollback (rather than commit)
	 *
	 * @return
	 */
	public HibernateTransaction withAutoRollback()
	{
		this.rollback = true;

		return this;
	}


	/**
	 * Set the automatic close action to commit (rather than rollback)
	 *
	 * @return
	 */
	public HibernateTransaction withAutoCommit()
	{
		this.rollback = false;

		return this;
	}


	public void commit()
	{
		if (closed.compareAndSet(false, true))
		{
			tx.commit();
		}
		else
		{
			throw new IllegalStateException("Cannot commit transaction: already closed!");
		}
	}


	public void rollback()
	{
		if (closed.compareAndSet(false, true))
		{
			tx.rollback();
		}
		else
		{
			throw new IllegalStateException("Cannot rollback transaction: already closed!");
		}
	}


	@Override
	public void close()
	{
		if (closed.compareAndSet(false, true))
		{
			if (rollback)
				tx.rollback();
			else
				tx.commit();
		}
	}
}
