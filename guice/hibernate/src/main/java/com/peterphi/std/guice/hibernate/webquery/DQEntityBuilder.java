package com.peterphi.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.hibernate.SessionFactory;

import java.util.HashMap;

/**
 * A simple Entity Builder
 */
@Singleton
public class DQEntityBuilder
{
	private final SessionFactory sessionFactory;

	@Inject
	public DQEntityBuilder(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	public DQEntity createEntity(final Class<?> clazz)
	{
		DQEntity entity = new DQEntity(sessionFactory,clazz,new HashMap<Class<?>, DQEntity>());
		return entity;
	}
}
