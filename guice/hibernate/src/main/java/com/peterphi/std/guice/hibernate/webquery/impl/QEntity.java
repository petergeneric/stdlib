package com.peterphi.std.guice.hibernate.webquery.impl;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

import javax.persistence.DiscriminatorValue;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QEntity
{
	private final Class<?> clazz;
	private String name;

	private Map<String, QProperty> properties = new HashMap<>();
	private Map<String, QRelation> relations = new HashMap<>();

	private List<QEntity> descendants = Collections.emptyList();


	public QEntity(Class<?> clazz)
	{
		this.clazz = clazz;
	}


	public Class<?> getEntityClass()
	{
		return clazz;
	}


	void parse(QEntityFactory entityFactory, ClassMetadata metadata, SessionFactoryImplementor sessionFactory)
	{
		this.name = metadata.getEntityName();

		final boolean[] nullability = metadata.getPropertyNullability();
		final String[] names = metadata.getPropertyNames();
		final Type[] types = metadata.getPropertyTypes();

		// Parse top-level properties
		parseFields(entityFactory, sessionFactory, null, nullability, names, types);

		// Add identifier property
		{
			final String name = metadata.getIdentifierPropertyName();
			final Type type = metadata.getIdentifierType();
			final Class<?> clazz = type.getReturnedClass();

			// Add an id property (N.B. may not work for embedded ids)
			properties.put(name, new QProperty(this, null, name, clazz, false));

			// If the identifier is a composite primary key then we should also add the composite fields
			if (type.isComponentType())
			{
				CompositeType composite = (CompositeType) type;

				parseFields(entityFactory, sessionFactory, name, composite);
			}
		}

		// Add links to descendants
		{
			final List<QEntity> descendants = entityFactory.getSubclasses(clazz);

			if (!descendants.isEmpty())
				this.descendants = descendants;
		}
	}


	private void parseFields(final QEntityFactory entityFactory,
	                         final SessionFactoryImplementor sessionFactory,
	                         final String prefix,
	                         final CompositeType composite)
	{
		parseFields(entityFactory,
		            sessionFactory,
		            prefix,
		            composite.getPropertyNullability(),
		            composite.getPropertyNames(),
		            composite.getSubtypes());
	}


	private void parseFields(final QEntityFactory entityFactory,
	                         final SessionFactoryImplementor sessionFactory,
	                         final String prefix,
	                         final boolean[] nullability,
	                         final String[] names,
	                         final Type[] types)
	{
		for (int i = 0; i < names.length; i++)
		{
			final Type type;

			// Transparently unwrap collections
			if (types[i].isCollectionType())
			{
				final CollectionType collectionType = (CollectionType) types[i];
				type = collectionType.getElementType(sessionFactory);
			}
			else
			{
				type = types[i];
			}

			final String name = names[i];
			final Class<?> clazz = type.getReturnedClass();
			final boolean nullable = nullability[i];

			// TODO is it also meaningful to add the parent composite type as a field too?
			// TODO if not we should have this as a separate if condition
			if (types[i].isComponentType())
			{
				CompositeType composite = (CompositeType) types[i];

				// This is a composite type, so add the composite types instead
				parseFields(entityFactory, sessionFactory, name, composite);
			}
			else if (type.isEntityType())
			{
				relations.put(name, new QRelation(this, prefix, name, entityFactory.get(clazz), nullable));
			}
			else
			{
				properties.put(name, new QProperty(this, prefix, name, clazz, nullable));
			}
		}
	}


	public String getName()
	{
		return this.name;
	}


	public String getDiscriminatorValue()
	{
		final DiscriminatorValue annotation = clazz.getAnnotation(DiscriminatorValue.class);

		if (annotation != null)
			return annotation.value();
		else
			return clazz.getName();
	}


	public Set<String> getPropertyNames()
	{
		return Collections.unmodifiableSet(this.properties.keySet());
	}


	public Set<String> getRelationNames()
	{
		return Collections.unmodifiableSet(this.relations.keySet());
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


	public List<QEntity> getSubEntities()
	{
		return descendants;
	}


	/**
	 * Return true if the underlying entity class is abstract
	 *
	 * @return
	 */
	public boolean isEntityClassAbstract()
	{
		return Modifier.isAbstract(clazz.getModifiers());
	}


	@Override
	public String toString()
	{
		return "QEntity{" +
		       "clazz=" + clazz +
		       ", name='" + name + '\'' +
		       ", properties=" + properties.values() +
		       ", relations=" + relations.values() +
		       '}';
	}
}
