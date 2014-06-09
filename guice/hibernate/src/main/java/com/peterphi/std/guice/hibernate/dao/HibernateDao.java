package com.peterphi.std.guice.hibernate.dao;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.database.dao.Dao;
import com.peterphi.std.guice.hibernate.exception.ReadOnlyTransactionException;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.ResultSetConstraint;
import com.peterphi.std.guice.hibernate.webquery.impl.QCriteriaBuilder;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntityFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

	protected Class<T> clazz;


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
		if (this.clazz != null)
			throw new IllegalStateException("Cannot call setTypeLiteral twice! Already has value " +
			                                this.clazz +
			                                ", will not overwrite with " +
			                                clazz.getRawType());

		// Guice sets a Class<? super T> but we know we can cast to Class<T> by convention
		this.clazz = (Class<T>) clazz.getRawType();
	}


	public Class<T> getEntityType()
	{
		return clazz;
	}

	/**
	 * Create a Dynamic query with the specified constraints
	 *
	 * @param constraints
	 * @param baseCriteria
	 * 		the base Criteria to add constraints to
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public Criteria createCriteria(ResultSetConstraint constraints, Criteria baseCriteria)
	{
		final QCriteriaBuilder builder = new QCriteriaBuilder(getQEntity());

		builder.addAll(constraints.getParameters());

		final Criteria criteria;

		if (baseCriteria != null)
			criteria = baseCriteria;
		else
			criteria = createCriteria();

		builder.append(criteria);

		return criteria;
	}


	public QEntity getQEntity()
	{
		return entityFactory.get(clazz);
	}


	@Override
	@Transactional(readOnly = true)
	public ConstrainedResultSet<T> findByUriQuery(ResultSetConstraint constraints)
	{
		return findByUriQuery(constraints, null);
	}


	@Transactional(readOnly = true)
	public ConstrainedResultSet<T> findByUriQuery(ResultSetConstraint constraints, Criteria base)
	{
		final Criteria criteria = createCriteria(constraints, base);

		final List<T> results = getList(criteria);

		return new ConstrainedResultSet<>(constraints, results);
	}


	protected String idProperty()
	{
		return getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName();
	}


	@Override
	public List<T> getByIds(final Collection<ID> ids)
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


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll()
	{
		return createCriteria().setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	}


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll(int offset, int limit)
	{
		Criteria criteria = createCriteria().setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
		criteria.setFetchSize(limit);

		return criteria.list();
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
	public void merge(T obj)
	{
		getWriteSession().merge(obj);
	}


	@Override
	public T getByUniqueProperty(final String propertyName, final Object value)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.eq(propertyName, value));
		criteria.setMaxResults(2);

		return uniqueResult(criteria);
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
	 * @throws HibernateException
	 * 		if there is more than one matching result
	 */
	protected T uniqueResult(Criteria criteria)
	{
		return clazz.cast(criteria.uniqueResult());
	}


	/**
	 * Convenience method to return a single, non-null instance that matches the query
	 *
	 * @param criteria
	 * 		a criteria created by this DAO
	 *
	 * @return the single result (N.B. never null)
	 *
	 * @throws HibernateException
	 * 		if the number of results was not exactly 1
	 */
	protected T one(Criteria criteria)
	{
		final T obj = uniqueResult(criteria);

		if (obj != null)
			return obj;
		else
			throw new NonUniqueResultException(0);
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
}
