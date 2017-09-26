package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.google.inject.Inject;
import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.module.logging.HibernateObservingInterceptor;
import com.peterphi.std.guice.hibernate.module.logging.HibernateSQLLogger;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JPASearchExecutor
{
	private static final Logger log = Logger.getLogger(JPASearchExecutor.class);

	protected final HibernateObservingInterceptor hibernateObserver;


	@Inject
	public JPASearchExecutor(HibernateObservingInterceptor hibernateObserver)
	{
		this.hibernateObserver = hibernateObserver;
	}


	/**
	 * Execute a search, returning a ConstrainedResultSet populated with the desired data (ID or Entity) with each piece of data
	 * optionally serialised using the supplied serialiser
	 *
	 * @param query
	 * 		the query to execute (including options like offset/limit, )
	 * @param strategy
	 * @param serialiser
	 * @param <T>
	 *
	 * @return
	 */
	public <T> ConstrainedResultSet<T> find(Supplier<JPAQueryBuilder> queryBuilderProvider,
	                                        final QEntity entity,
	                                        final WebQuery query,
	                                        JPASearchStrategy strategy,
	                                        Function<?, ?> serialiser)
	{
		final HibernateSQLLogger statementLog;

		if (query.isLogSQL())
			statementLog = hibernateObserver.startSQLLogger();
		else
			statementLog = null;

		// Build a view of the query based on
		JPAQueryBuilder builder = queryBuilderProvider.get();
		builder.forWebQuery(query);


		// If the auto strategy is in play, take into account what's being fetched back as well as whether there are any explicit collection joins or fetches
		if (strategy == null || strategy == JPASearchStrategy.AUTO)
		{
			if (StringUtils.equals(query.getFetch(), "id"))
			{
				strategy = JPASearchStrategy.ENTITY_WRAPPED_ID;
			}
			else
			{
				if (builder.hasCollectionJoin() || builder.hasCollectionFetch())
				{
					strategy = JPASearchStrategy.ID_THEN_QUERY_ENTITY;
				}
				else
				{
					strategy = JPASearchStrategy.ENTITY;
				}
			}
		}

		Long total = null;
		List list;
		switch (strategy)
		{
			case ID:
			{
				list = builder.selectIDs();

				if (query.isComputeSize())
					total = builder.selectCount();

				break;
			}
			case ENTITY_WRAPPED_ID:
			{
				list = builder.selectIDs();

				if (query.isComputeSize())
					total = builder.selectCount();

				// Transform the IDs into entity objects with the ID field populated
				list = (List) list.stream().map(entity:: newInstanceWithId).collect(Collectors.toList());

				break;
			}
			case ENTITY:
			{
				// First, query for the total results (if desired)
				if (query.isComputeSize())
					total = builder.selectCount();

				// TODO could we use ScrollableResults if there are collection joins? pagination would be tricky

				list = builder.selectEntity();

				break;
			}
			case ID_THEN_QUERY_ENTITY:
			{
				// First, query for the IDs (and the total results if desired)
				list = builder.selectIDs();

				if (query.isComputeSize())
					total = builder.selectCount();

				// Now re-query to retrieve the entities
				builder = queryBuilderProvider.get();
				builder.forIDs(query, list);

				if (!list.isEmpty())
					list = builder.selectEntity();

				break;
			}
			default:
				throw new NotImplementedException("Search Strategy " + strategy + " not yet implemented");
		}

		// If a serialiser has been supplied,
		if (serialiser != null)
			list = (List) list.stream().map(serialiser).collect(Collectors.toList());

		ConstrainedResultSet resultset = new ConstrainedResultSet<>(query, list);

		if (statementLog != null)
			resultset.setSql(statementLog.getAllStatements());

		if (total != null)
			resultset.setTotal(total);

		return (ConstrainedResultSet<T>) resultset;
	}
}
