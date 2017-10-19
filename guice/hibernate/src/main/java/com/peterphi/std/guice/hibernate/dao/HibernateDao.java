package com.peterphi.std.guice.hibernate.dao;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.annotations.Reconfigurable;
import com.peterphi.std.guice.database.annotation.LargeTable;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.database.dao.Dao;
import com.peterphi.std.guice.hibernate.exception.ReadOnlyTransactionException;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.JPAQueryBuilder;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.JPASearchExecutor;
import com.peterphi.std.guice.hibernate.webquery.impl.jpa.JPASearchStrategy;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.QueryHints;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The default implementation of a Dao for Hibernate; often it is necessary to extend this to produce richer queries<br />
 * This DAO can be used in two main ways:
 * <ol>
 * <li>With singleton instances of this class created for each entity (where only simple CRUD methods are required)</li>
 * <li>With subclasses of this HibernateDao implementing additional queries (often with corresponding extensions to the Dao
 * type)</li>
 * </ol>
 *
 * @param <T>
 * 		entity type
 * @param <ID>
 * 		primary key type
 */
public class HibernateDao<T, ID extends Serializable> implements Dao<T, ID>
{
	@Inject
	private SessionFactory sessionFactory;

	@Inject
	QEntityFactory entityFactory;

	@Inject(optional = true)
	@Named("hibernate.perform-separate-id-query-for-large-tables")
	@Reconfigurable
	@Doc("If true then URI queries on @LargeTable annotated entities will result in a query to retrieve ids followed by a query to retrieve data. This provides a massive speedup with some databases (e.g. 43x with SQL Server) on large tables when using joins (default true)")
	boolean performSeparateIdQueryForLargeTables = true;

	protected Class<T> clazz;

	protected boolean isLargeTable = false;

	@Inject
	public JPASearchExecutor searchExecutor;


	public HibernateDao()
	{
	}


	/**
	 * Called by guice to provide the Class associated with T
	 *
	 * @param clazz
	 * 		The TypeLiteral associated with T (the entity type)
	 */
	@Inject
	@SuppressWarnings("unchecked")
	public void setTypeLiteral(TypeLiteral<T> clazz)
	{
		if (clazz == null)
			throw new IllegalArgumentException("Cannot set null TypeLiteral on " + this);
		if (this.clazz != null && !this.clazz.equals(clazz.getRawType()))
			throw new IllegalStateException("Cannot call setTypeLiteral twice! Already has value " +
			                                this.clazz +
			                                ", will not overwrite with " +
			                                clazz.getRawType());

		// Guice sets a Class<? super T> but we know we can cast to Class<T> by convention
		this.clazz = (Class<T>) clazz.getRawType();

		isLargeTable = this.clazz.isAnnotationPresent(LargeTable.class);
	}


	public JPAQueryBuilder<T, ID> createQueryBuilder()
	{
		return new JPAQueryBuilder<>(getSession(), getQEntity());
	}


	public Class<T> getEntityType()
	{
		return clazz;
	}


	public QEntity getQEntity()
	{
		return entityFactory.get(clazz);
	}


	@Deprecated
	protected String idProperty()
	{
		return getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName();
	}


	@Override
	@Deprecated
	public List<T> getByIds(final Collection<ID> ids)
	{
		return getListById(ids);
	}


	@Override
	public List<T> getListById(final Collection<ID> ids)
	{
		if (ids.isEmpty())
			return Collections.emptyList();

		if (ids instanceof List)
			return getListById((List<ID>) ids);
		else
			return getListById(new ArrayList<>(ids));
	}


	@Transactional(readOnly = true)
	public List<T> getListById(final List<ID> ids)
	{
		if (ids.isEmpty())
			return Collections.emptyList();
		else if (ids.size() == 1)
			return Collections.singletonList(getById(ids.get(0))); // Optimise getting one entity so it's more cache friendly
		else
		{
			// Build a query so that we can get all entities and their eager fetches in one go
			// N.B. this is necessary because multifetch doesn't support Entity Graphs
			final JPAQueryBuilder<T, ID> builder = createQueryBuilder();

			builder.forWebQuery(new WebQuery());

			return builder.selectCustom((cb, criteriaQuery, root, qb) -> {
				criteriaQuery.select(root);

				final Path idProperty = root.get(root.getModel().getId(root.getModel().getIdType().getJavaType()));

				// Convert the ID list into a list of ID matching Predicates
				final Predicate[] predicates = new Predicate[ids.size()];
				int i = 0;
				for (ID id : ids)
					predicates[i++] = cb.equal(idProperty, id);

				// OR them together and set them as the conditional
				criteriaQuery.where(getCriteriaBuilder().or(predicates));

				// Apply all default fetch joins for this entity
				qb.applyFetches();
			});
		}
	}


	@Override
	@Transactional(readOnly = true)
	public T getById(ID id)
	{
		if (id == null)
			throw new IllegalArgumentException("Must supply an id to retrieve!");

		Map<String, Object> hints = Collections.singletonMap(QueryHints.FETCHGRAPH, getQEntity().getDefaultGraph(getSession()));

		return getSession().find(clazz, id, hints);
	}


	@Override
	@Transactional(readOnly = true)
	public List<T> getAll()
	{
		return getList(new WebQuery());
	}


	@Override
	@Transactional
	public void deleteById(ID id)
	{
		if (id == null)
			throw new IllegalArgumentException("Cannot delete a null id!");

		final T obj = getById(id);

		delete(obj);
	}


	@Override
	@Transactional
	public void delete(T obj)
	{
		getWriteSession().delete(obj);
	}


	@Override
	@Transactional
	public void saveOrUpdate(T obj)
	{
		getWriteSession().saveOrUpdate(obj);
	}


	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public ID save(T obj)
	{
		return (ID) getWriteSession().save(obj);
	}


	@Override
	@Transactional
	public void update(T obj)
	{
		getWriteSession().update(obj);
	}


	@Override
	@Transactional
	public T merge(T obj)
	{
		return (T) getWriteSession().merge(obj);
	}


	@Override
	@Transactional(readOnly = true)
	public T getByUniqueProperty(final String propertyName, final Object value)
	{
		return uniqueResult(new WebQuery().eq(propertyName, value));
	}


	protected Session getSession()
	{
		return sessionFactory.getCurrentSession();
	}


	/**
	 * Get a session and confirm that it is permitted to perform write operations<br />
	 *
	 * @return
	 *
	 * @throws ReadOnlyTransactionException
	 * 		if we cannot perform writes in this session
	 */
	protected Session getWriteSession()
	{
		final Session session = getSession();

		if (session.isDefaultReadOnly())
		{
			throw new ReadOnlyTransactionException("Cannot perform write operation on " + clazz + " in a read-only transaction");
		}
		else
		{
			return session;
		}
	}


	/**
	 * Create a {@link Criteria} instance for the entity type of this DAO, matching this entity and any subclasses/implementors
	 *
	 * @return The criteria instance for manipulation and execution
	 *
	 * @deprecated use JPA2 Criteria with {@link #createCriteriaQuery()}, Hibernate has deprecated their old criteria query and it
	 * will produce runtime warnings
	 */
	@Deprecated
	protected Criteria createCriteria()
	{
		return getSession().createCriteria(clazz);
	}


	/**
	 * Create a JPA2 CriteriaQuery, which should have constraints generated using {@link #getCriteriaBuilder()}
	 *
	 * @param <O>
	 * 		the return type of the query
	 *
	 * @return
	 */
	protected <O> CriteriaQuery<O> createCriteriaQuery()
	{
		return (CriteriaQuery) getCriteriaBuilder().createQuery();
	}


	/**
	 * Create a JPA2 CriteriaQuery, which should have constraints generated using the methods in {@link #getCriteriaBuilder()}
	 *
	 * @param <O>
	 * 		the return type of the query
	 * @param clazz
	 * 		the return type of the query
	 *
	 * @return
	 */
	protected <O> CriteriaQuery<O> createCriteriaQuery(Class<O> clazz)
	{
		return getCriteriaBuilder().createQuery(clazz);
	}


	protected CriteriaBuilder getCriteriaBuilder()
	{
		return getSession().getCriteriaBuilder();
	}


	/**
	 * Create a {@link Query} instance for the given HQL query string.
	 *
	 * @param hql
	 * 		The HQL query
	 *
	 * @return The query instance for manipulation and execution
	 */
	protected Query createQuery(String hql)
	{
		return getSession().createQuery(hql);
	}


	/**
	 * Execute a Criteria search, returning the results as a checked list
	 *
	 * @param criteria
	 * 		a criteria created by this DAO
	 *
	 * @return The list of matched query results, implicitly cast to a List of this DAO type
	 *
	 * @throws HibernateException
	 * 		Indicates a problem either translating the criteria to SQL, executing the SQL or processing the SQL results.
	 */
	@SuppressWarnings("unchecked")
	protected List<T> getList(Criteria criteria)
	{
		return criteria.list();
	}


	/**
	 * Convenience method to return a single instance that matches the query, or null if the query returns no results.
	 *
	 * @param criteria
	 * 		a criteria created by this DAO
	 *
	 * @return the single result or <tt>null</tt>
	 *
	 * @throws IllegalStateException
	 * 		if there is more than one matching result
	 */
	protected T uniqueResult(Criteria criteria)
	{
		return clazz.cast(criteria.uniqueResult());
	}


	protected T uniqueResult(WebQuery query)
	{
		final ConstrainedResultSet<T> results = find(query);

		return results.uniqueResult();
	}


	/**
	 * Execute a Criteria-based search, returning the results as a checked list
	 *
	 * @param query
	 * 		a query that returns an object compatible with the entity type of this DAO
	 *
	 * @return The list of matched query results, implicitly cast to a List of this DAO type
	 *
	 * @throws HibernateException
	 * 		Indicates a problem either translating the criteria to SQL, executing the SQL or processing the SQL results.
	 */
	@SuppressWarnings("unchecked")
	protected List<T> getList(Query query)
	{
		return query.list();
	}


	protected List<T> getList(WebQuery query)
	{
		return find(query).getList();
	}


	/**
	 * Execute a Criteria-based search, returning the results as a checked list of primary key types
	 *
	 * @param criteria
	 * 		the criteria (note, its projection must have been set to Projections.id())
	 *
	 * @return
	 *
	 * @throws HibernateException
	 * 		Indicates a problem either translating the criteria to SQL, executing the SQL or processing the SQL results.
	 */
	@SuppressWarnings("unchecked")
	protected List<ID> getIdList(Criteria criteria)
	{
		return criteria.list();
	}


	@Transactional(readOnly = true)
	public List<ID> getIdList(WebQuery query)
	{
		return findIds(query).getList();
	}


	/**
	 * Execute a Query search, returning the results as a checked list of primary key types
	 *
	 * @param query
	 * 		the query (note, its projection must have been set to the primary key)
	 *
	 * @return
	 *
	 * @throws HibernateException
	 * 		Indicates a problem either translating the criteria to SQL, executing the SQL or processing the SQL results.
	 */
	@SuppressWarnings("unchecked")
	protected List<ID> getIdList(Query query)
	{
		return query.list();
	}


	/**
	 * Convenience method to return a single instance that matches the query, or null if the query returns no results.
	 *
	 * @param query
	 * 		a query that returns an object compatible with the entity type of this DAO
	 *
	 * @return the single result or <tt>null</tt>
	 *
	 * @throws HibernateException
	 * 		if there is more than one matching result
	 */
	protected T uniqueResult(Query query)
	{
		return clazz.cast(query.uniqueResult());
	}


	/**
	 * Return the SessionFactory, useful for dynamic query building, etc.<br />
	 * <strong>NOTE: Using this this is potentially disruptive to the normal way of writing applications in our guice
	 * framework</strong>
	 *
	 * @return
	 */
	protected SessionFactory getSessionFactory()
	{
		return this.sessionFactory;
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// WebQueryDefinition query methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public ConstrainedResultSet<ID> findIdsByUriQuery(final WebQuery query)
	{
		return (ConstrainedResultSet<ID>) find(query, JPASearchStrategy.ID);
	}


	@Override
	public ConstrainedResultSet<T> findByUriQuery(final WebQuery query)
	{
		return find(query);
	}


	@Override
	public ConstrainedResultSet<T> find(final WebQuery query)
	{
		return find(query, JPASearchStrategy.AUTO);
	}


	@Override
	public ConstrainedResultSet<T> find(final WebQuery query, JPASearchStrategy strategy)
	{
		return find(query, strategy, null);
	}


	@Override
	public ConstrainedResultSet<ID> findIds(final WebQuery query)
	{
		return find(query, JPASearchStrategy.ID, null);
	}


	@Override
	@Transactional(readOnly = true)
	public <RT> ConstrainedResultSet<RT> find(final WebQuery query, JPASearchStrategy strategy, Function<?, RT> serialiser)
	{
		// If necessary, swap the AUTO strategy for ID_THEN_QUERY_ENTITY if this entity is annotated with @LargeTable
		// TODO replace this annotation with something that allows forcing the strategy on a per-entity basis?
		if (performSeparateIdQueryForLargeTables && isLargeTable && (strategy == null || strategy == JPASearchStrategy.AUTO))
			strategy = JPASearchStrategy.ID_THEN_QUERY_ENTITY;

		return searchExecutor.find(getQEntity(), query, strategy, serialiser);
	}


	/**
	 * Get a list of IDs matching a WebQuery
	 *
	 * @param constraints
	 *
	 * @return
	 */
	public Collection<ID> getIds(final WebQuery constraints)
	{
		return (Collection<ID>) find(constraints, JPASearchStrategy.ID).getList();
	}
}
