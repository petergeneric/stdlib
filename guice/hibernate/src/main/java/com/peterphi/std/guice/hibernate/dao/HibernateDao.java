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
import com.peterphi.std.guice.hibernate.module.logging.HibernateObservingInterceptor;
import com.peterphi.std.guice.hibernate.module.logging.HibernateSQLLogger;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.impl.HQLBuilder;
import com.peterphi.std.guice.hibernate.webquery.impl.HQLProjection;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	@Inject(optional = true)
	@Named("hibernate.database-allows-order-by-without-select")
	@Reconfigurable
	@Doc("If true then the HQL emitted from WebQueries will be allowed to include an ORDER BY without those columns being explicitly listed in the SELECT. Recommended this be false for HSQLDB and true for all other database engines.")
	boolean databaseAllowsOrderByWithoutSelect = true;

	@Inject
	HibernateObservingInterceptor hibernateObserver;

	protected Class<T> clazz;

	protected boolean isLargeTable = false;


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


	public Class<T> getEntityType()
	{
		return clazz;
	}


	public QEntity getQEntity()
	{
		return entityFactory.get(clazz);
	}


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
			return new ArrayList<>();

		return getList(createCriteria().add(Restrictions.in(idProperty(), ids)));
	}


	@Override
	@Transactional(readOnly = true)
	public T getById(ID id)
	{
		if (id == null)
			throw new IllegalArgumentException("Must supply an id to retrieve!");

		return clazz.cast(getSession().get(clazz, id));
	}


	@Override
	@Transactional(readOnly = true)
	public List<T> getAll()
	{
		return getList(createCriteria().setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY));
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
	 */
	protected Criteria createCriteria()
	{
		return getSession().createCriteria(clazz);
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
		final ConstrainedResultSet<T> results = findByUriQuery(query);

		if (results.getList().size() == 0)
		{
			return null;
		}
		else if (results.getList().size() == 1)
		{
			return clazz.cast(results.getList().get(0));
		}
		else
		{
			throw new IllegalStateException("Expected 0 or 1 result, got " + results.getList().size());
		}
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
		return findByUriQuery(query).getList();
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


	protected List<ID> getIdList(WebQuery query)
	{
		return (List<ID>) findByUriQuery(query).getList();
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


	/**
	 * @param query
	 * 		the criteria
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	@Override
	public ConstrainedResultSet<T> findByUriQuery(final WebQuery query)
	{
		final HibernateSQLLogger statementLog;

		if (query.isLogSQL())
			statementLog = hibernateObserver.startSQLLogger();
		else
			statementLog = null;


		try
		{
			// Optionally execute the count query
			final Long total;
			if (query.constraints.computeSize)
			{
				// Re-run the query to obtain the size
				final Query countQuery = toRowCountQuery(query);

				final Number size = (Number) countQuery.uniqueResult();

				total = size.longValue();
			}
			else
			{
				total = null;
			}


			// Now fetch back the data
			final ConstrainedResultSet<T> resultset;
			{
				if (total == null || total > 0)
				{
					final Query criteria = createQuery(query);

					final List<T> results = getList(criteria);

					resultset = new ConstrainedResultSet<>(query, results);
				}
				else
				{
					// We know there were no results because the collection size was computed
					resultset = new ConstrainedResultSet<>(query, Collections.emptyList());
				}
			}

			resultset.setTotal(total);

			// If we have an active statement log then expose the statements that have been prepared
			if (statementLog != null)
			{
				resultset.setSql(statementLog.getAllStatements());
			}

			return resultset;
		}
		finally
		{
			if (statementLog != null)
				statementLog.close();
		}
	}


	/**
	 * Convert a WebQuery to a Criteria, automatically indirecting through an id query if the entity is annotated with {@link
	 * LargeTable}
	 *
	 * @param constraints
	 * 		the constraints to apply.
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	protected Query createQuery(WebQuery constraints)
	{
		// Optionally treat large tables differently (works around a SQL Server performance issue)
		// See documentation on toGetByIdCriteria for more detail
		if (isLargeTable && performSeparateIdQueryForLargeTables)
			return toGetByIdQuery(constraints);
		else
			return toSimpleQuery(constraints);
	}


	/**
	 * SQL Server Performance Workaround
	 * <p>
	 * Given a normal query, execute the search component <strong>but not the data retrieval</strong>, instead retrieving only
	 * the Primary Keys of the entities to return (paginated and in the correct order).
	 * <p>
	 * This is to side-step an issue in SQL Server where it expands all the joins against a large table into a temporary table
	 * before then applying filtering to that temporary table (not applying any of the filters to the original table) - this
	 * results in a massive temporary table being created and then almost immediately being filtered down to a very small number
	 * of rows.
	 * <p>
	 * If the initial query is only asking for Primary Keys then SQL Server is able to optimise the query correctly. N.B. this
	 * could also be implemented as a subquery to avoid a double-query however it'd be necessary to be able to convert a WebQuery
	 * into a DetachedCriteria in order to do this (and support is not yet written for this)
	 *
	 * @param constraints
	 *
	 * @return
	 */
	protected Query toGetByIdQuery(WebQuery constraints)
	{
		// Retrieve the primary keys separately from the data
		final Collection<ID> ids = getIds(constraints);

		if (ids.size() > 0)
		{
			final HQLBuilder byIdBuilder = toCriteriaBuilder(constraints);

			// N.B. we do not need the constraints / pagination because we have already evaluated them
			byIdBuilder.clearConstraints();
			byIdBuilder.clearPagination();

			// Re-apply subclass constraints if defined
			if (StringUtils.isNotEmpty(constraints.constraints.subclass))
				byIdBuilder.addClassConstraint(Arrays.asList(constraints.constraints.subclass.split(",")));

			// Add a custom constraint that the ID must be one of the values we've already determined
			byIdBuilder.addIdInConstraint(ids);

			return byIdBuilder.toHQL(this :: createQuery);
		}
		else
		{
			final HQLBuilder emptyQueryBuilder = toCriteriaBuilder(new WebQuery());

			// There were no results for this query, hibernate can't handle Restrictions.in(empty) so we must make sure no results come back
			emptyQueryBuilder.addAlwaysFalseConstraint();

			// Hint that we don't want any results
			emptyQueryBuilder.limit(0);

			return emptyQueryBuilder.toHQL(this :: createQuery);
		}
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
		final List ret = toGetIdQuery(constraints).list();

		if (ret.isEmpty())
			return Collections.emptyList(); // Empty list
		else if (ret.get(0).getClass().isArray())
		{
			// In the case where there are ORDER BY statements and the database needs ORDER BYs to be SELECTed, we may be returned a bunch of data in addition to the IDs
			// List of columns, we only care about the first column in each row
			return (List<ID>) ret.stream().map(r -> Array.get(r, 0)).map(id -> (ID) id).collect(Collectors.toList());
		}
		else
		{
			// Simple list of IDs
			return (List<ID>) ret;
		}
	}


	protected Query toGetIdQuery(WebQuery query)
	{
		// Encode the WebQuery and add the constraints
		final HQLBuilder builder = toCriteriaBuilder(query);

		builder.setProjection(HQLProjection.IDS);

		return builder.toHQL(this :: createQuery);
	}


	protected Query toRowCountQuery(WebQuery constraints)
	{
		// Encode the WebQuery and add the constraints
		final HQLBuilder builder = toCriteriaBuilder(constraints).clearPagination().clearOrder();

		builder.setProjection(HQLProjection.COUNT);

		Query hql = builder.toHQL(this :: createQuery);

		// Discount offset/limit
		hql.setFirstResult(0);
		hql.setMaxResults(Integer.MAX_VALUE);

		return hql;
	}


	/**
	 * Create a straight conversion of the provided ResultSetConstraint.
	 *
	 * @param query
	 * 		the constraints (optional, if null then no restrictions will be appended to the base criteria)
	 *
	 * @return
	 */
	protected Query toSimpleQuery(WebQuery query)
	{
		// Encode the WebQuery and add the constraints
		final HQLBuilder builder = toCriteriaBuilder(query);

		return builder.toHQL(this :: createQuery);
	}


	/**
	 * Convert a WebQuery to a QCriteriaBuilder representing the same query
	 *
	 * @param query
	 *
	 * @return
	 */
	protected HQLBuilder toCriteriaBuilder(final WebQuery query)
	{
		HQLBuilder builder = new HQLBuilder(getQEntity());

		builder.setDatabaseAllowsOrderByWithoutSelect(this.databaseAllowsOrderByWithoutSelect);

		builder.addWebQuery(query);

		return builder;
	}
}
