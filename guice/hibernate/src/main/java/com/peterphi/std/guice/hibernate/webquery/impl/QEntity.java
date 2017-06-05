package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.database.annotation.SearchFieldAlias;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntitySchema;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QEntity
{
	private static final Logger log = Logger.getLogger(QEntity.class);

	private final Class<?> clazz;
	private String name;

	private Map<String, QProperty> properties = new HashMap<>();
	private Map<String, String> aliases = new HashMap<>(0);
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

			// If the identifier is a composite primary key then we should add the composite fields
			if (type.isComponentType())
			{
				CompositeType composite = (CompositeType) type;

				parseFields(entityFactory, sessionFactory, name, composite);
			}
			else
			{
				// The identifier is not a composite type, so just add the field directly
				properties.put(name, new QProperty(this, null, name, clazz, false));
			}
		}

		// Add field aliases defined on the class
		for (SearchFieldAlias alias : clazz.getAnnotationsByType(SearchFieldAlias.class))
		{
			aliases.put(alias.name(), alias.aliasOf());
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


	public void parseEmbeddable(final QEntityFactory qEntityFactory,
	                            final SessionFactoryImplementor sessionFactory,
	                            final ComponentType ct)
	{
		final String[] names = ct.getPropertyNames();
		final Type[] types = ct.getSubtypes();
		final boolean[] nullable = ct.getPropertyNullability();

		// unknown what to do...
		parseFields(qEntityFactory, sessionFactory, null, nullable, names, types);
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
			final boolean isCollection;

			// Transparently unwrap collections
			if (types[i].isCollectionType())
			{
				final CollectionType collectionType = (CollectionType) types[i];
				type = collectionType.getElementType(sessionFactory);

				isCollection = true;
			}
			else
			{
				type = types[i];
				isCollection = false;
			}

			final String name = names[i];
			final Class<?> clazz = type.getReturnedClass();
			final boolean nullable = nullability[i];

			// TODO is it also meaningful to add the parent composite type as a field too?
			// TODO if not we should have this as a separate if condition
			if (types[i].isComponentType() && !isCollection)
			{
				CompositeType composite = (CompositeType) types[i];

				final String newPrefix;

				if (prefix != null)
					newPrefix = prefix + ":" + name;
				else
					newPrefix = name;

				// This is a composite type, so add the composite types instead
				parseFields(entityFactory, sessionFactory, newPrefix, composite);
			}
			else if (type.isEntityType() || isCollection)
			{
				final QRelation relation;
				if (type.isEntityType())
				{
					relation = new QRelation(this, prefix, name, entityFactory.get(clazz), nullable, isCollection);
				}
				else if (isCollection && type instanceof ComponentType)
				{
					ComponentType ct = (ComponentType) type;

					relation = new QRelation(this, prefix, name, entityFactory.getEmbeddable(clazz, ct), nullable, isCollection);
				}
				else
				{
					log.warn("Unknown Collection type: " + type + " with name " + name + " within " + clazz + " - ignoring");
					relation = null;
				}

				if (relation != null)
				{
					relations.put(name, relation);

					// Set up a special property to allow constraining the collection size
					if (isCollection)
						properties.put(name + ":size", new QSizeProperty(relations.get(name)));
				}
			}
			else
			{
				final String newPrefix;

				if (prefix != null)
					newPrefix = prefix + ":" + name;
				else
					newPrefix = name;


				properties.put(newPrefix, new QProperty(this, prefix, name, clazz, nullable));
			}
		}
	}


	public String getName()
	{
		final Table table = this.clazz.getAnnotation(Table.class);
		final Entity entity = this.clazz.getAnnotation(Entity.class);

		if (table != null && !StringUtils.isBlank(table.name()))
			return table.name();
		else if (entity != null && !StringUtils.isBlank(entity.name()))
			return entity.name();
		else
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


	public Set<String> getAliasNames()
	{
		return Collections.unmodifiableSet(this.aliases.keySet());
	}


	public boolean hasProperty(String name)
	{
		return properties.containsKey(name);
	}


	/**
	 * Modify a list of segments by replacing any defined alias on this entity. If there is no such destination alias then does
	 * nothing<br />
	 * This is designed to allow underlying database schema changes without changing the query API exposed to users
	 *
	 * @param segments
	 * 		some path optionally including an aliased name (e.g. "asset.parentId")
	 *
	 * @return some new path (e.g. "asset.parent.id")
	 */
	public void fixupPathUsingAliases(final LinkedList<String> segments)
	{
		if (aliases.isEmpty())
			return; // No transformation necessary or possible

		final String remainingPath = segments.stream().collect(Collectors.joining("."));

		for (Map.Entry<String, String> entry : aliases.entrySet())
		{
			final String from = entry.getKey();
			final String to = entry.getValue();

			if (StringUtils.equals(remainingPath, from) || StringUtils.startsWith(remainingPath, from + "."))
			{
				final String newPath = to + remainingPath.substring(from.length());

				segments.clear();
				segments.addAll(Arrays.asList(StringUtils.split(newPath, '.')));

				return;
			}
		}
	}


	public boolean hasRelation(String name)
	{
		return relations.containsKey(name);
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


	public WQEntitySchema encode()
	{
		WQEntitySchema obj = new WQEntitySchema();

		obj.name = getName();
		obj.discriminator = getDiscriminatorValue();

		if (this.descendants.size() > 0)
			obj.childEntityNames = descendants.stream().map(QEntity:: getName).collect(Collectors.toList());

		obj.properties = properties.values().stream().map(QProperty:: encode).collect(Collectors.toList());
		obj.properties.addAll(relations.values().stream().map(QRelation:: encode).collect(Collectors.toList()));

		return obj;
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
		       "clazz=" +
		       clazz +
		       ", name='" +
		       name +
		       '\'' +
		       ", properties=" +
		       properties.values() +
		       ", relations=" +
		       relations.values() +
		       ", aliases=" +
		       aliases.values() +
		       '}';
	}


	public QEntity getSubEntity(final String discriminator)
	{
		if (StringUtils.equals(getDiscriminatorValue(), discriminator))
			return this;
		else
			for (QEntity entity : getSubEntities())
				if (StringUtils.equals(entity.getDiscriminatorValue(), discriminator))
					return entity;

		throw new IllegalArgumentException("Unknown subclass with discriminator: " + discriminator);
	}


	public QEntity getCommonSubclass(final List<String> discriminators)
	{
		Map<Class, QEntity> entities = discriminators
				                               .stream()
				                               .map(this :: getSubEntity)
				                               .collect(Collectors.toMap(e -> e.clazz, e -> e));

		Collection<Class> classes = entities.keySet();

		for (Class potentialSuperclass : classes)
		{
			boolean suitableForAll = true;
			for (Class test : classes)
			{
				suitableForAll = suitableForAll && test.isAssignableFrom(potentialSuperclass);
			}

			// If this is a suitable superclass for all classes we have tested then we have a winner!
			if (suitableForAll)
				return entities.get(potentialSuperclass);
		}

		// Found no more specific subclass
		return this;
	}
}
