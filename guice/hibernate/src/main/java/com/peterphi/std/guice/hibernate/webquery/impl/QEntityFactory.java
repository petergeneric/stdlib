package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQSchemas;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class QEntityFactory
{
	private static final Logger log = Logger.getLogger(QEntityFactory.class);

	private final Map<Class<?>, QEntity> entities = new HashMap<>();
	private final SessionFactoryImplementor sessionFactory;


	@Inject
	public QEntityFactory(final SessionFactory sessionFactory)
	{
		this.sessionFactory = (SessionFactoryImplementor) sessionFactory;

		if (log.isDebugEnabled())
			log.debug("Known entities: " + sessionFactory.getAllClassMetadata().keySet());

		// Pre-construct the QEntity instances for all known entities
		for (ClassMetadata metadata : sessionFactory.getAllClassMetadata().values())
		{
			final Class clazz = metadata.getMappedClass();

			if (clazz != null && !Modifier.isAbstract(clazz.getModifiers()))
				get(clazz);
		}
	}


	public List<QEntity> getSubclasses(Class<?> superclass)
	{
		List<QEntity> subclasses = new ArrayList<>();

		for (ClassMetadata meta : sessionFactory.getAllClassMetadata().values())
		{
			final Class<?> clazz = meta.getMappedClass();

			if (clazz != null && !Modifier.isAbstract(clazz.getModifiers()) && superclass.isAssignableFrom(clazz))
				subclasses.add(get(clazz));
		}

		return subclasses;
	}


	public QEntity get(Class<?> clazz)
	{
		if (!entities.containsKey(clazz))
		{
			log.debug("Begin create QEntity " + clazz);
			final ClassMetadata metadata = sessionFactory.getClassMetadata(clazz);

			QEntity entity = new QEntity(clazz);
			entities.put(clazz, entity);

			entity.parse(this, metadata, sessionFactory);

			log.debug("End create QEntity " + clazz);
		}

		return entities.get(clazz);
	}


	public WQSchemas encode()
	{
		WQSchemas obj = new WQSchemas();

		obj.entities = getAll().stream().map(QEntity:: encode).collect(Collectors.toList());

		return obj;
	}


	public List<QEntity> getAll()
	{
		// Pre-emptively process all entities
		sessionFactory.getAllClassMetadata().values().stream().map(ClassMetadata:: getMappedClass).forEach(this :: get);

		return new ArrayList<>(entities.values());
	}
}
