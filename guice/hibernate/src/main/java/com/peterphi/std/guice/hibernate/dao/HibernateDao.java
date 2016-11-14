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
import com.peterphi.std.guice.hibernate.webquery.impl.QCriteriaBuilder;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
		// Optionally execute the count query
		final Long total;
		if (query.constraints.computeSize)
		{
			// Re-run the query to obtain the size
			final Criteria countCriteria = toRowCountCriteria(query);

			final Number size = (Number) countCriteria.uniqueResult();

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
				final Criteria criteria = createCriteria(query);

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


		return resultset;
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
	protected Criteria createCriteria(WebQuery constraints)
	{
		// Optionally treat large tables differently (works around a SQL Server performance issue)
		if (isLargeTable && performSeparateIdQueryForLargeTables)
			return toGetByIdCriteria(constraints);
		else
			return toSimpleCriteria(constraints);
	}


	protected Criteria toGetByIdCriteria(WebQuery constraints)
	{
		// Retrieve the primary keys separately from the data
		final List<ID> ids = toGetIdCriteria(constraints).list();

		final Criteria criteria = createCriteria();

		if (ids.size() > 0)
		{
			criteria.add(Restrictions.in(idProperty(), ids));

			// Append joins, orders and discriminators (but not the constraints, we have already evaluated them)
			toCriteriaBuilder(constraints).appendTo(criteria, false, false);

			return criteria;
		}
		else
		{
			// There were no results for this query, hibernate can't handle Restrictions.in(empty) so we must make sure no results come back
			criteria.add(Restrictions.sqlRestriction("(0=1)"));

			// Hint that we don't want any results
			criteria.setMaxResults(0);

			return criteria;
		}
	}


	protected Criteria toGetIdCriteria(WebQuery constraints)
	{
		final Criteria criteria = toSimpleCriteria(constraints);

		criteria.setProjection(Projections.id());

		return criteria;
	}


	protected Criteria toRowCountCriteria(WebQuery constraints)
	{
		final Criteria criteria = toSimpleCriteria(constraints);

		// Discount offset/limit
		criteria.setFirstResult(0);
		criteria.setMaxResults(Integer.MAX_VALUE);

		// Request the row count
		criteria.setProjection(Projections.rowCount());

		return criteria;
	}

/*
	protected DetachedCriteria toDetachedCriteria(WebQuery query)
	{
		final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityType());

		// Encode the WebQuery and add the constraints
		toCriteriaBuilder(query).appendTo(criteria);

		return criteria;
	}*/


	/**
	 * Create a straight conversion of the provided ResultSetConstraint.
	 *
	 * @param query
	 * 		the constraints (optional, if null then no restrictions will be appended to the base criteria)
	 *
	 * @return
	 */
	protected Criteria toSimpleCriteria(WebQuery query)
	{
		final Criteria criteria = createCriteria();

		// Encode the WebQuery and add the constraints
		toCriteriaBuilder(query).appendTo(criteria);

		return criteria;
	}


	/**
	 * Convert a WebQuery to a QCriteriaBuilder representing the same query
	 *
	 * @param query
	 *
	 * @return
	 */
	protected QCriteriaBuilder toCriteriaBuilder(final WebQuery query)
	{
		final QCriteriaBuilder builder = new QCriteriaBuilder(getQEntity()).offset(query.getOffset()).limit(query.getLimit());

		// Add the sort order
		for (WQOrder order : query.orderings)
			builder.addOrder(builder.getProperty(order.field), order.isAsc());

		if (StringUtils.isNotBlank(query.constraints.subclass))
			builder.addClass(Arrays.asList(query.constraints.subclass.split(",")));

		builder.addConstraints(query.constraints.constraints);

		return builder;
	}
}
