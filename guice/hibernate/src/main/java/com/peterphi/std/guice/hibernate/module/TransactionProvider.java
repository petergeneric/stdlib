package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Session;
import org.hibernate.Transaction;

class TransactionProvider implements Provider<Transaction>
{
	private final Provider<Session> sessionProvider;


	@Inject
	public TransactionProvider(final Provider<Session> sessionProvider)
	{
		this.sessionProvider = sessionProvider;
	}


	@Override
	public Transaction get()
	{
		final Session session = sessionProvider.get();

		return session.getTransaction();
	}
}
