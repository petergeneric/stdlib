package com.peterphi.std.guice.hibernate.webquery.impl;

import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.Map;

public class QEntity
{
	private final Class<?> clazz;

	private Map<String, QProperty> properties = new HashMap<>();
	private Map<String, QRelation> relations = new HashMap<>();


	public QEntity(Class<?> clazz)
	{
		this.clazz = clazz;
	}


	void parse(QEntityFactory entityFactory, ClassMetadata metadata)
	{
		final String[] names = metadata.getPropertyNames();
		final Type[] types = metadata.getPropertyTypes();

		for (int i = 0; i < names.length; i++)
		{
			final String name = names[i];
			final Type type = types[i];
			final Class<?> clazz = type.getReturnedClass();

			if (!type.isCollectionType())
			{
				if (type.isEntityType())
				{
					relations.put(name, new QRelation(this, name, entityFactory.get(clazz)));
				}
				else
				{
					properties.put(name, new QProperty(this, name, clazz));
				}
			}
		}

		// Add identifier
		{
			final String name = metadata.getIdentifierPropertyName();
			final Type type = metadata.getIdentifierType();
			final Class<?> clazz = type.getReturnedClass();

			properties.put(name, new QProperty(this, name, clazz));
		}
	}


	public QProperty getProperty(String name)
	{
		final QProperty property = properties.get(name);

		if (property != null)
			return property;
		else
			throw new IllegalArgumentException("Unknown property " +
			                                   name +
			                                   " on " +
			                                   this.clazz.getSimpleName() +
			                                   ", expected one of " +
			                                   properties.keySet());
	}


	public QRelation getRelation(String name)
	{
		final QRelation relation = relations.get(name);

		if (relation != null)
			return relation;
		else
			throw new IllegalArgumentException("Unknown relation " +
			                                   name +
			                                   " on " +
			                                   this.clazz.getSimpleName() +
			                                   ", expected one of " +
			                                   relations.keySet());
	}
}
