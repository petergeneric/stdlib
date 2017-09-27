package com.peterphi.std.guice.hibernate.webquery.impl;

import com.peterphi.std.guice.restclient.jaxb.webqueryschema.WQEntitySchema;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class QEntity
{
	private static final Logger log = Logger.getLogger(QEntity.class);

	private final Class<?> clazz;
	private String name;

	private Map<String, QProperty> properties = new HashMap<>();
	private Map<String, QRelation> relations = new HashMap<>();

	private List<QEntity> descendants = Collections.emptyList();

	private EntityType<?> metamodelEntity;

	// Populated to let us get/set the ID of an entity dynamically
	private Field idField;
	private Method idSetMethod;
	private Method idGetMethod;

	private Set<String> defaultExpand;
	private Set<String> eagerRelations = new HashSet<>(0);

	private Map<String, Attribute> nonEntityRelations = new HashMap<>(0);


	public QEntity(Class<?> clazz)
	{
		this.clazz = clazz;
	}


	public Class<?> getEntityClass()
	{
		return clazz;
	}


	public EntityType<?> getMetamodelEntity()
	{
		return this.metamodelEntity;
	}


	void parse(QEntityFactory entityFactory, EntityType<?> metadata, SessionFactoryImplementor sessionFactory)
	{
		this.metamodelEntity = metadata;
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
		}

		// Add links to descendants
		{
			final List<QEntity> descendants = entityFactory.getSubclasses(clazz);

			if (!descendants.isEmpty())
				this.descendants = descendants;
		}

		// Figure out the id method/field
		findReflectionIdFieldOrMethods();
	}


	private List<Field> getAllFields(Class clazz)
	{
		List<Field> fields = new ArrayList<>();

		while (clazz != null)
		{
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}

		return fields;
	}


	/**
	 * Use reflection to find the Field or Methods for getting and setting the id property ({@link Id} annotated) of this entity
	 */
	private void findReflectionIdFieldOrMethods()
	{
		try
		{
			Field idField = null;

			for (Field field : getAllFields(clazz))
			{
				if (field.isAnnotationPresent(Id.class))
				{
					idField = field;
					break;
				}
			}

			if (idField != null)
			{
				if (idField.isAccessible() ||
				    (Modifier.isPublic(idField.getModifiers()) && !Modifier.isFinal(idField.getModifiers())))
				{
					this.idField = idField;
				}
				else
				{
					final String getterName = "get" + idField.getName();
					final String setterName = "set" + idField.getName();

					if (log.isTraceEnabled())
					{
						log.trace("Looking for getter with name: " + getterName);
						log.trace("Looking for setter with name: " + setterName);
					}

					for (Method method : clazz.getMethods())
					{
						if (this.idGetMethod == null && StringUtils.equalsIgnoreCase(method.getName(), getterName))
						{
							this.idGetMethod = method;
						}
						else if (this.idSetMethod == null && StringUtils.equalsIgnoreCase(method.getName(), setterName))
						{
							this.idSetMethod = method;
						}
					}

					if (idGetMethod == null || idSetMethod == null)
						throw new RuntimeException("Field " +
						                           idField.toGenericString() +
						                           " exists and is annotated with @Id but is not accessible and could not resolve getter and setter methods by the same name");
				}
			}

			if (this.idField == null && this.idGetMethod == null && this.idSetMethod == null)
			{
				for (Method method : clazz.getMethods())
				{
					if (method.isAnnotationPresent(Id.class))
					{
						this.idGetMethod = method;
						break;
					}
				}

				if (idGetMethod != null)
				{
					final String setMethodName = idGetMethod.getName().replaceFirst("^get", "set");

					for (Method method : clazz.getMethods())
					{
						if (StringUtils.equals(setMethodName, method.getName()))
						{
							this.idSetMethod = method;
							break;
						}
					}
				}

				if (idGetMethod == null || idSetMethod == null)
					throw new IllegalArgumentException("Could not find a public getter and setter annotated with @Id!");
			}
		}
		catch (Throwable t)
		{
			log.warn("Could not find Id field/methods for entity class " + clazz, t);
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


	protected boolean isEagerFetch(Attribute<?, ?> attribute)
	{
		if (attribute.isCollection() || attribute.isAssociation())
		{
			Member member = attribute.getJavaMember();

			if (member instanceof AnnotatedElement)
			{
				final AnnotatedElement el = (AnnotatedElement) member;

				final FetchType fetchType;
				if (el.isAnnotationPresent(OneToMany.class))
					fetchType = el.getAnnotation(OneToMany.class).fetch();
				else if (el.isAnnotationPresent(ManyToOne.class))
					fetchType = el.getAnnotation(ManyToOne.class).fetch();
				else if (el.isAnnotationPresent(OneToOne.class))
					fetchType = el.getAnnotation(OneToOne.class).fetch();
				else if (el.isAnnotationPresent(ElementCollection.class))
					fetchType = el.getAnnotation(ElementCollection.class).fetch();
				else if (el.isAnnotationPresent(ManyToMany.class))
					fetchType = el.getAnnotation(ManyToMany.class).fetch();
				else
					return false;

				return (fetchType == FetchType.EAGER);
			}
		}

		return false;
	}


	public Set<String> getEagerFetch()
	{
		if (this.defaultExpand == null)
		{
			Set<String> populate = new HashSet<>();

			getEagerFetch(new HashSet<>(), new Stack<>(), populate);

			this.defaultExpand = Collections.unmodifiableSet(populate);
		}

		return this.defaultExpand;
	}


	protected void getEagerFetch(Set<QEntity> visited, Stack<String> path, Set<String> populate)
	{
		if (visited.contains(this))
			return;
		else
			visited.add(this);

		for (String eagerRelation : eagerRelations)
		{
			// Always add the relation, even if it's not a part of the QEntity parse structure (e.g. may be a Map or some other ElementCollection)
			if (path.isEmpty())
				populate.add(eagerRelation);
			else
				populate.add(StringUtils.join(path, '.') + "." + eagerRelation);

			// If this is a known relation then expand it further
			if (hasRelation(eagerRelation))
			{
				path.push(eagerRelation);

				final QEntity entity = getRelation(eagerRelation).getEntity();

				entity.getEagerFetch(visited, path, populate);

				path.pop();
			}
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
			SingularAttribute<?, ?> attr = (SingularAttribute<?, ?>) attribute;
			EmbeddableType<?> ct = (EmbeddableType<?>) attr.getType();

			parseEmbeddable(entityFactory, sessionFactory, prefix, ct);
		}
		else if (isCollection || attribute.isAssociation())
		{
			final boolean isEagerFetch = isEagerFetch(attribute);

			final QRelation relation;
			if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED &&
			    attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC)
			{
				if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ELEMENT_COLLECTION)
				{
					relation = new QRelation(this, prefix, name, entityFactory.get(clazz), nullable, isEagerFetch, isCollection);
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
						                         isEagerFetch,
						                         isCollection);
					}
					else if (plural.getElementType().getPersistenceType() == Type.PersistenceType.BASIC)
					{
						// Ignore this altogether. We should probably come up with a way of querying this relationship in the future
						relation = null;

						// Record the relation so we can use it in the future to determine if a relation is a collection for query optimisation
						nonEntityRelations.put(name, attribute);

						if (log.isDebugEnabled())
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

				relation = new QRelation(this,
				                         prefix,
				                         name,
				                         entityFactory.getEmbeddable(clazz, ct),
				                         nullable,
				                         isEagerFetch,
				                         isCollection);
			}
			else
			{
				// N.B. this fallback catches collections we don't natively support but that we have some support for (i.e. for fetch joining against)
				// As result, we will not be able to record them as a QRelation, but we can record them as a non-entity relation
				relation = null;

				// Record the relation so we can use it in the future to determine if a relation is a collection for query optimisation
				nonEntityRelations.put(name, attribute);

				if (log.isDebugEnabled())
					log.debug("Unknown Collection type: " +
					          attribute.getPersistentAttributeType() +
					          " " +
					          attribute +
					          " with name " +
					          name +
					          " within " +
					          clazz +
					          " - ignoring");
			}

			if (relation != null)
			{
				relations.put(name, relation);

				// Set up a special property to allow constraining the collection size
				if (isCollection)
					properties.put(name + ":size", new QSizeProperty(relations.get(name)));
			}

			if (isEagerFetch)
			{
				this.eagerRelations.add(attribute.getName());
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


	public boolean hasProperty(String name)
	{
		return properties.containsKey(name);
	}


	public boolean hasRelation(String name)
	{
		return relations.containsKey(name);
	}


	public boolean hasNonEntityRelation(final String name)
	{
		return nonEntityRelations.containsKey(name);
	}


	public boolean isNonEntityRelationCollection(final String name)
	{
		if (!hasNonEntityRelation(name))
			throw new IllegalArgumentException("No such non-entity relation: " + name + " on " + this);
		else
			return nonEntityRelations.get(name).isCollection();
	}


	/**
	 * Create a new instance of this entity, setting only the ID field
	 *
	 * @param id
	 *
	 * @return
	 */
	public Object newInstanceWithId(final Object id)
	{
		try
		{
			final Object o = clazz.newInstance();

			if (idField != null)
			{
				idField.set(o, id);
			}
			else if (idSetMethod != null)
			{
				idSetMethod.invoke(o, id);
			}
			else
			{
				throw new RuntimeException("No id field/setter method found!");
			}

			return o;
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Cannot create new instance of " +
			                           clazz +
			                           " with ID " +
			                           id +
			                           " (of type " +
			                           id.getClass() +
			                           ") populated!", e);
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
		       properties.keySet() +
		       ", relations=" +
		       relations.keySet() +
		       ", defaultFetch=" +
		       defaultExpand +
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
