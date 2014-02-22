package com.peterphi.std.guice.database.dao;

import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraint;
import org.hibernate.Criteria;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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
	 * @return
	 */
	public Class<T> getEntityType();

	/**
	 * Execute a Dynamic query using the specified constraints
	 *
	 * @param constraints
	 *
	 * @return
	 *
	 * @deprecated use findbyUriQuery instead
	 */
	@Deprecated
	public List<T> getConstraintResults(ResultSetConstraint constraints);

	/**
	 * Execute a Dynamic query using the specified constraints, returning the result as a ConstrainedResultSet
	 *
	 * @param constraints
	 *
	 * @return
	 */
	public ConstrainedResultSet<T> findByUriQuery(ResultSetConstraint constraints);

	/**
	 * Execute a Dynamic query using the specified constraints, returning the result as a ConstrainedResultSet
	 *
	 * @param constraints
	 * @param base
	 * 		the base criteria to use (the constraints will be ANDed with this Criteria
	 *
	 * @return
	 */
	public ConstrainedResultSet<T> findByUriQuery(ResultSetConstraint constraints, Criteria base);

	/**
	 * Retrieve every object accessible through this DAO
	 *
	 * @return
	 */
	public List<T> getAll();

	/**
	 * Retrieve a page from the list of every object accessible through this DAO
	 *
	 * @param offset
	 * 		the first result to return
	 * @param limit
	 * 		the maximum number of results (the "page size")
	 *
	 * @return
	 */
	public List<T> getAll(int offset, int limit);

	/**
	 * Retrieve an item by its primary key
	 *
	 * @param id
	 *
	 * @return the item (or null if it is not present)
	 */
	public T getById(ID id);

	/**
	 * Query the database for all items with the given primary keys (convenience method for an ORred id query)
	 *
	 * @param ids
	 * 		a collection (may be empty) of primary keys
	 *
	 * @return any items whose id are contained within <code>ids</code>. May be empty if no matches were found. May be smaller
	 * than
	 * <code>ids</code>
	 */
	public List<T> getByIds(final Collection<ID> ids);

	/**
	 * Delete an item by its primary key
	 *
	 * @param id
	 */
	public void deleteById(ID id);

	/**
	 * Delete an item from the database
	 *
	 * @param obj
	 */
	public void delete(T obj);

	/**
	 * Create or Update an item in the database
	 *
	 * @param obj
	 */
	public void saveOrUpdate(T obj);

	/**
	 * Save a new item in the database, returning its primary key
	 *
	 * @param obj
	 *
	 * @return the primary key of the newly saved object
	 */
	public ID save(T obj);

	/**
	 * Update an existing item in the database
	 *
	 * @param obj
	 */
	public void update(T obj);
}
