package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.database.annotation.SearchFieldAlias;
import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntitySchema;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
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


	void parse(QEntityFactory entityFactory, EntityType<?> metadata, SessionFactoryImplementor sessionFactory)
	{
		this.name = metadata.getName();

		for (Attribute<?, ?> attribute : metadata.getAttributes())
		{
			parseFields(entityFactory, sessionFactory, null, attribute);
		}

		// Parse top-level properties

		// Add identifier property
		{
			if (!metadata.hasSingleIdAttribute())
				throw new IllegalArgumentException("@IdClass Entity not supported! " + metadata.getJavaType());

			Type idType = metadata.getIdType();

			switch (idType.getPersistenceType())
			{
				case BASIC:
					break; // No action necessary, will be processed like a normal field
				case EMBEDDABLE:
				{
					EmbeddableType<?> emb = (EmbeddableType<?>) idType;

					parseEmbeddable(entityFactory, sessionFactory, "id", emb);
					break;
				}
				default:
					throw new IllegalArgumentException("Cannot handle id type: " + idType.getPersistenceType() + ": " + idType);
			}
/*
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
			}*/
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


	public void parseEmbeddable(final QEntityFactory entityFactory,
	                            final SessionFactoryImplementor sessionFactory,
	                            final String prefix,
	                            final EmbeddableType<?> type)
	{
		// Make sure the entity factory sees this embeddable
		entityFactory.getEmbeddable(type.getJavaType(), type);

		for (Attribute<?, ?> attribute : type.getAttributes())
		{
			parseFields(entityFactory, sessionFactory, prefix, attribute);
		}
	}


	private void parseFields(final QEntityFactory entityFactory,
	                         final SessionFactoryImplementor sessionFactory,
	                         final String prefix,
	                         final Attribute<?, ?> attribute)
	{
		final String name = attribute.getName();
		//final javax.persistence.metamodel.Type<?> type;
		final Class<?> clazz;
		final boolean isCollection;
		final boolean nullable;

		// Transparently unwrap collections
		if (attribute.isCollection())
		{
			isCollection = true;
			nullable = false;

			//type = ((PluralAttribute<?, ?, ?>) attribute).getElementType();
			clazz = ((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType();
		}
		else
		{
			isCollection = false;
			//type = attribute.getDeclaringType();
			nullable = (attribute instanceof SingularAttribute) ? ((SingularAttribute) attribute).isOptional() : false;
			clazz = attribute.getJavaType();
		}

		// TODO is it also meaningful to add the parent composite type as a field too?
		// TODO if not we should have this as a separate if condition
		if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED && !isCollection)
		{
			final String newPrefix;

			if (prefix != null)
				newPrefix = prefix + ":" + name;
			else
				newPrefix = name;

			SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
			EmbeddableType<?> ct = (EmbeddableType<?>) attr.getType();

			parseEmbeddable(entityFactory, sessionFactory, prefix, ct);
		}
		else if (isCollection || attribute.isAssociation())
		{
			final QRelation relation;
			if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED &&
			    attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC)
			{
				if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ELEMENT_COLLECTION)
				{
					relation = new QRelation(this, prefix, name, entityFactory.get(clazz), nullable, isCollection);
				}
				else
				{
					PluralAttribute<?, ?, ?> plural = (PluralAttribute<?, ?, ?>) attribute;

					if (plural.getElementType().getPersistenceType() == Type.PersistenceType.EMBEDDABLE)
					{
						final EmbeddableType<?> ct = (EmbeddableType<?>) plural.getElementType();

						relation = new QRelation(this,
						                         prefix,
						                         name,
						                         entityFactory.getEmbeddable(ct.getJavaType(), ct),
						                         nullable,
						                         isCollection);
					}
					else if (plural.getElementType().getPersistenceType() == Type.PersistenceType.BASIC)
					{
						// Ignore this altogether. We should probably come up with a way of querying this relationship in the future
						relation = null;

						log.debug("Ignoring BASIC ElementCollection " +
						          plural.getCollectionType() +
						          " of " +
						          plural.getElementType().getJavaType() +
						          " " +
						          plural.getName());
					}
					else
					{

						throw new IllegalArgumentException("Cannot handle ElementCollection of " +
						                                   plural.getElementType().getJavaType() +
						                                   " - type " +
						                                   plural.getElementType().getPersistenceType());
					}
				}
			}
			else if (isCollection && attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED)
			{
				SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
				EmbeddableType<?> ct = (EmbeddableType<?>) attr.getType();

				relation = new QRelation(this, prefix, name, entityFactory.getEmbeddable(clazz, ct), nullable, isCollection);
			}
			else
			{
				log.warn("Unknown Collection type: " +
				         attribute.getPersistentAttributeType() +
				         " " +
				         attribute +
				         " with name " +
				         name +
				         " within " +
				         clazz +
				         " - ignoring");
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
