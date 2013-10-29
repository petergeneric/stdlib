package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class QEntityFactory
{
	private static final Logger log = Logger.getLogger(QEntityFactory.class);

	private final Map<Class<?>, QEntity> entities = new HashMap<>();
	private final SessionFactory sessionFactory;


	@Inject
	public QEntityFactory(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;

		log.debug("Known entities: " + sessionFactory.getAllClassMetadata().keySet());
	}


	public QEntity get(Class<?> clazz)
	{
		if (!entities.containsKey(clazz))
		{
			log.debug("Begin create QEntity " + clazz);
			final ClassMetadata metadata = sessionFactory.getClassMetadata(clazz);

			QEntity entity = new QEntity(clazz);
			entities.put(clazz, entity);

			entity.parse(this, metadata);

			log.debug("End create QEntity " + clazz);
		}

		return entities.get(clazz);
	}
}
