package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.module.logging.HibernateObservingInterceptor;
import com.peterphi.std.guice.hibernate.module.logging.HibernateSQLLogger;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.hibernate.webquery.impl.QEntity;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.util.tracing.Tracing;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class JPASearchExecutor
{
	private static final Logger log = Logger.getLogger(JPASearchExecutor.class);

	/**
	 * If true, will treat all WebQuery instances as if they had set computeSize to true<br />
	 * This is mainly present to allow unit tests to check that computeSize works with various different WebQuery calls without having to duplicate
	 */
	public static boolean ALWAYS_COMPUTE_SIZE = false;

	@Inject
	HibernateObservingInterceptor hibernateObserver;

	@Inject
	SessionFactory sessionFactory;


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
	public <T> ConstrainedResultSet<T> find(final QEntity entity,
	                                        final WebQuery query,
	                                        JPASearchStrategy strategy,
	                                        Function<?, ?> serialiser)
	{
		final String traceOperationId = Tracing.log("WebQuery:exec", () -> query.toString());

		final HibernateSQLLogger statementLog;

		if (query.isLogSQL())
			statementLog = hibernateObserver.startSQLLogger(traceOperationId);
		else
			statementLog = null;

		try
		{

			// Build a view of the query based on
			JPAQueryBuilder builder = new JPAQueryBuilder(sessionFactory.getCurrentSession(), entity);
			builder.forWebQuery(query);

			// First, compute the total size if requested
			final Long total;
			if (ALWAYS_COMPUTE_SIZE || query.isComputeSize())
			{
				JPAQueryBuilder countBuilder = new JPAQueryBuilder(sessionFactory.getCurrentSession(), entity);
				countBuilder.forWebQuery(query);

				total = countBuilder.selectCount();
			}
			else
			{
				total = null;
			}

			// If the auto strategy is in play, take into account what's being fetched back as well as whether there are any explicit collection joins or fetches
			if (strategy == null || strategy == JPASearchStrategy.AUTO)
			{
				if (StringUtils.equals(query.getFetch(), "id"))
				{
					strategy = JPASearchStrategy.ENTITY_WRAPPED_ID;
				}
				else
				{
					// If we're constraining by (or fetching) a collection AND we have a limit/offset, first get PKs for the entities and then fetch the entities
					// This is necessary for correct pagination because the SQL resultset will have more than one row per entity, and our offset/limit is based on entity
					if ((query.getLimit() > 0 || query.getOffset() > 0) && (builder.hasCollectionJoin() || builder.hasCollectionFetch()))
					{
						strategy = JPASearchStrategy.ID_THEN_QUERY_ENTITY;
					}
					else
					{
						strategy = JPASearchStrategy.ENTITY;
					}
				}
			}

			List list;
			switch (strategy)
			{
				case ID:
				{
					list = builder.selectIDs();

					break;
				}
				case ENTITY_WRAPPED_ID:
				{
					list = builder.selectIDs();

					// Transform the IDs into entity objects with the ID field populated
					list = (List) list.stream().map(entity:: newInstanceWithId).collect(Collectors.toList());

					break;
				}
				case ENTITY:
				{
					// TODO could we use ScrollableResults if there are collection joins? pagination would be tricky

					list = builder.selectEntity();

					break;
				}
				case ID_THEN_QUERY_ENTITY:
				{
					// First, query for the IDs (and the total results if desired)
					list = builder.selectIDs();

					// Now re-query to retrieve the entities
					builder = new JPAQueryBuilder(sessionFactory.getCurrentSession(), entity);
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

			if (statementLog != null && query.isLogSQL())
				resultset.setSql(statementLog.getAllStatements());

			if (total != null)
				resultset.setTotal(total);

			Tracing.logOngoing(traceOperationId,
			                   "WebQuery:exec:result",
			                   () -> "size=" + resultset.getList().size() + ", total=" + total);

			return (ConstrainedResultSet<T>) resultset;
		}
		finally {
			if (statementLog != null)
				statementLog.close();
		}
	}
}
