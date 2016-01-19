package com.peterphi.std.guice.hibernate.webquery.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.peterphi.std.guice.database.annotation.SearchFieldAlias;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntitySchema;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QEntity
{
	private final Class<?> clazz;
	private String name;

	private Map<String, QProperty> properties = new HashMap<>();
	private Map<String, String> aliases = new HashMap<>(0);
	private Map<String, QRelation> relations = new HashMap<>();

	private List<QEntity> descendants = Collections.emptyList();

	private final Cache<String, QPropertyPathBuilder> builderCache = CacheBuilder.newBuilder()
	                                                                             .weakKeys()
	                                                                             .weakValues()
	                                                                             .initialCapacity(0)
	                                                                             .build();


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
			if (types[i].isComponentType())
			{
				CompositeType composite = (CompositeType) types[i];

				final String newPrefix;

				if (prefix != null)
					newPrefix = prefix + "." + name;
				else
					newPrefix = name;

				// This is a composite type, so add the composite types instead
				parseFields(entityFactory, sessionFactory, newPrefix, composite);
			}
			else if (type.isEntityType())
			{
				relations.put(name, new QRelation(this, prefix, name, entityFactory.get(clazz), nullable));

				// Set up a special property to allow constraining the collection size
				properties.put(name + ":size", new QSizeProperty(relations.get(name)));
			}
			else
			{
				properties.put(name, new QProperty(this, prefix, name, clazz, nullable));
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


	public boolean hasAlias(String name)
	{
		return aliases.containsKey(name);
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


	public QPropertyPathBuilder getPath(final String path)
	{
		QPropertyPathBuilder cached = builderCache.getIfPresent(path);

		if (cached == null)
		{
			final QPropertyPathBuilder builder = new QPropertyPathBuilder();

			cached = getPath(builder, path);

			builderCache.put(path, cached);
		}

		return cached;
	}


	public QPropertyPathBuilder getPath(final QPropertyPathBuilder builder, String path)
	{
		final int firstDot = path.indexOf('.');

		final boolean terminal = (firstDot == -1);

		final String head = terminal ? path : path.substring(0, firstDot);
		final String tail = terminal ? null : path.substring(firstDot + 1);

		if (hasAlias(head))
		{
			final String newPath;

			if (terminal)
				newPath = getAlias(head);
			else
				newPath = getAlias(head) + "." + tail;

			return getPath(builder, newPath);
		}
		else if (hasProperty(head))
		{
			if (tail != null)
				throw new IllegalArgumentException("Found property " + head + " but there are other path components: " + tail);

			builder.append(getProperty(head));

			return builder;
		}
		else if (hasRelation(head))
		{
			final QRelation relation = getRelation(head);

			builder.append(getRelation(head));

			return relation.getEntity().getPath(builder, tail);
		}
		else
		{
			final Set<String> expected = new HashSet<>(getPropertyNames());
			expected.addAll(getRelationNames());
			expected.addAll(getAliasNames());

			throw new IllegalArgumentException("Relationship path error: got " +
			                                   head +
			                                   ", expected one of: " + expected);
		}
	}


	/**
	 * Retrieve the resolution of a defined alias on this entity. If there is no such destination alias then return null<br />
	 * This is designed to allow underlying database schema changes without changing the query API exposed to users
	 *
	 * @param name
	 * 		some aliased name (e.g. "assetId")
	 *
	 * @return some alias destination (e.g. "asset.id")
	 */
	public String getAlias(String name)
	{
		return aliases.get(name);
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
		       "clazz=" + clazz +
		       ", name='" + name + '\'' +
		       ", properties=" + properties.values() +
		       ", relations=" + relations.values() +
		       ", aliases=" + aliases.values() +
		       '}';
	}
}
