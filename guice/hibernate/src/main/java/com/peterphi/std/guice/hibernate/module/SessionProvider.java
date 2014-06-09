package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

class SessionProvider implements Provider<Session>
{
	private final Provider<SessionFactory> sessionFactoryProvider;


	@Inject
	public SessionProvider(final Provider<SessionFactory> sessionFactoryProvider)
	{
		this.sessionFactoryProvider = sessionFactoryProvider;
	}


	@Override
	public Session get()
	{
		return sessionFactoryProvider.get().getCurrentSession();
	}
}
