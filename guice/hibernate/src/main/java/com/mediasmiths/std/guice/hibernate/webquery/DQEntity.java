package com.mediasmiths.std.guice.hibernate.webquery;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.Map;

public class DQEntity
{
	private final SessionFactory sessionFactory;
	private final Class<?> clazz;
	private final ClassMetadata classMetadata;

	private final Map<String, DQField> fields = new HashMap<String, DQField>();
	private final Map<String, DQEntity> entities = new HashMap<String, DQEntity>();


	public DQEntity(final SessionFactory sessionFactory, final Class<?> clazz, Map<Class<?>, DQEntity> preexisting)
	{
		this.sessionFactory = sessionFactory;
		this.clazz = clazz;

		this.classMetadata = this.sessionFactory.getClassMetadata(this.clazz);

		preexisting.put(clazz, this); // We're the DQEntity for this class

		populate(preexisting);
	}

	protected void populate(Map<Class<?>, DQEntity> preexisting)
	{
		final String[] names = classMetadata.getPropertyNames();
		final Type[] types = classMetadata.getPropertyTypes();

		for (int i = 0; i < names.length; i++)
		{
			final String name = names[i];
			final Type type = types[i];
			final Class<?> clazz = type.getReturnedClass();

			// We do not consider collection types
			if (!type.isCollectionType())
			{
				if (type.isEntityType())
				{
					DQEntity entity = preexisting.get(clazz);

					if (entity == null)
						entity = new DQEntity(sessionFactory, clazz, preexisting);

					// Record that we know about a joinable entity
					entities.put(name, entity);
				}
				else
				{
					final DQField field = new DQField(new DQType(clazz), name);

					fields.put(field.getName(), field);
				}
			}
		}

		// Add the identifier field (it's treated separately from all other fields)
		{
			final String name = classMetadata.getIdentifierPropertyName();
			final Type type = classMetadata.getIdentifierType();
			final Class<?> clazz = type.getReturnedClass();

			final DQField id = new DQField(new DQType(clazz), name);
			fields.put(id.getName(), id);
		}
	}

	public DQField getField(final String field)
	{
		return fields.get(field);
	}

	public DQEntity getEntity(final String field)
	{
		return entities.get(field);
	}

	public String getEntityName()
	{
		return classMetadata.getEntityName();
	}

	//convience method
	protected Criteria createCriteria()
	{
		return sessionFactory.getCurrentSession().createCriteria(clazz);
	}
}
