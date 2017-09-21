package com.peterphi.std.guice.database.dao;

import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.JPASearchStrategy;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * An abstraction over the store/retrieve semantics of hibernate to allow a higher-level pattern of access
 *
 * @param <T>
 * 		the entity type
 * @param <ID>
 * 		the primary key type for the entity
 */
public interface Dao<T, ID extends Serializable>
{
	/**
	 * Get the type of Entity this DAO processes
	 *
	 * @return the entity type
	 */
	public Class<T> getEntityType();

	/**
	 * Given a query, return the IDs of the matching entities, returning the result as a ConstrainedResultSet of this entity's id
	 * type
	 *
	 * @param query
	 *
	 * @return
	 */
	ConstrainedResultSet<ID> findIdsByUriQuery(WebQuery query);

	/**
	 * Execute a Dynamic query using the specified constraints, returning the result as a ConstrainedResultSet of this entity type
	 *
	 * @param constraints
	 * 		the constraints to apply
	 *
	 * @return a resultset containing the requested number of results
	 */
	ConstrainedResultSet<T> findByUriQuery(WebQuery constraints);

	/**
	 * Execute a query, returning entities
	 * @param constraints
	 * @return
	 */
	ConstrainedResultSet<T> find(WebQuery constraints);

	/**
	 * Execute a query, using the named strategy (defaults to AUTO if null)
	 * @param constraints
	 * @param strategy strategy, or null to default to AUTO
	 * @return
	 */
	ConstrainedResultSet<T> find(WebQuery constraints, JPASearchStrategy strategy);

	/**
	 * Execute a query, using the ID strategy
	 * @param constraints
	 * @return
	 */
	ConstrainedResultSet<ID> findIds(WebQuery constraints);

	/**
	 * Execute a query, using the named strategy (defaults to AUTO if null) and optionally serialising the resulting rows using the provided serialiser
	 * @param constraints
	 * @param strategy strategy, or null to default to AUTO
	 *                 @param serialiser the serialiser function to use to convert each row returned to the resulting resultset
	 * @return
	 */
	<RT> ConstrainedResultSet<RT> find(WebQuery constraints, JPASearchStrategy strategy, Function<?,RT> serialiser);

	/**
	 * Retrieve every object accessible through this DAO
	 *
	 * @return all entities of this type in the database
	 */
	public List<T> getAll();

	/**
	 * Retrieve an item by its primary key
	 *
	 * @param id
	 * 		the id of an entity
	 *
	 * @return the item (or null if it is not present)
	 *
	 * @see org.hibernate.Session#get(Class, java.io.Serializable)
	 */
	public T getById(ID id);

	/**
	 * @see #getListById(Collection)
	 * @deprecated use {@link #getListById(Collection)} instead
	 */
	public List<T> getByIds(final Collection<ID> ids);

	/**
	 * Query the database for all items with the given primary keys (convenience method for an ORred id query)
	 *
	 * @param ids
	 * 		a collection of primary keys (may be empty)
	 *
	 * @return any items whose id are contained within <code>ids</code>. May be empty if no matches were found. May be smaller
	 * than <code>ids</code>. May be ordered differently from <code>ids</code>
	 */
	public List<T> getListById(final Collection<ID> ids);

	/**
	 * Delete an item by its primary key
	 *
	 * @param id
	 * 		the id of an entity
	 */
	public void deleteById(ID id);

	/**
	 * Delete an item from the database
	 *
	 * @param obj
	 * 		the entity
	 *
	 * @see org.hibernate.Session#delete(Object)
	 */
	public void delete(T obj);

	/**
	 * Create or Update an item in the database
	 *
	 * @param obj
	 * 		the entity
	 *
	 * @see org.hibernate.Session#saveOrUpdate(Object)
	 */
	public void saveOrUpdate(T obj);

	/**
	 * Save a new item in the database, returning its primary key
	 *
	 * @param obj
	 * 		the entity
	 *
	 * @return the primary key of the newly saved object
	 *
	 * @see org.hibernate.Session#save(Object)
	 */
	public ID save(T obj);

	/**
	 * Update an existing item in the database
	 *
	 * @param obj
	 * 		the entity
	 *
	 * @see org.hibernate.Session#update(Object)
	 */
	public void update(T obj);

	/**
	 * Merge an unmanaged entity into a (new or existing) managed entity
	 *
	 * @param obj
	 * 		the entity
	 *
	 * @return a Hibernate entity version of obj
	 *
	 * @see org.hibernate.Session#merge(Object)
	 */
	public T merge(T obj);


	/**
	 * Convenience method which retrieves an entity by the equality of a single property (may return null if no entities match)
	 *
	 * @param propertyName
	 * 		the property name (MUST be a simple property)
	 * @param value
	 * 		the property value
	 *
	 * @return the entity, or null if no results were found
	 *
	 * @throws org.hibernate.HibernateException
	 * 		if more than one property is returned
	 */
	public T getByUniqueProperty(String propertyName, Object value);
}
