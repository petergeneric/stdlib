package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

import com.peterphi.std.NotImplementedException;
import com.peterphi.std.guice.hibernate.module.logging.HibernateObservingInterceptor;
import com.peterphi.std.guice.hibernate.module.logging.HibernateSQLLogger;
import com.peterphi.std.guice.hibernate.webquery.ConstrainedResultSet;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JPASearchExecutor
{
	private final HibernateObservingInterceptor hibernateObserver;
	private final Supplier<JPAQueryBuilder> queryBuilderProvider;


	public JPASearchExecutor(final Supplier<JPAQueryBuilder> queryBuilderProvider,
	                         HibernateObservingInterceptor hibernateObserver)
	{
		this.queryBuilderProvider = queryBuilderProvider;
		this.hibernateObserver = hibernateObserver;
	}


	// TODO some way to record the strategy?
	// Strategies:
	//   - Fetch ID
	//   - Fetch Entity-wrapped ID (for easier serialisation)
	//   - Fetch Entity
	//   - Fetch ID and then retrieve Entity
	// Options:
	//  - Compute total
	//  - Log SQL
	//  - Pass through serialiser


	public <T> ConstrainedResultSet<T> find(final WebQuery query, JPASearchStrategy strategy, final Function<?, ?> serialiser)
	{
		final HibernateSQLLogger statementLog;

		if (query.isLogSQL())
			statementLog = hibernateObserver.startSQLLogger();
		else
			statementLog = null;

		// Build a view of the query based on
		JPAQueryBuilder builder = queryBuilderProvider.get();
		builder.forWebQuery(query);


		// TODO be smart about what strategy to use based on not just whether there are query collection joins but whether the fetch strategy includes collection joins
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
			case ENTITY_WRAPPED_ID:
				throw new NotImplementedException("Cannot currently construct Entity-wrapped ID select!");
			case ID:
			{
				list = builder.selectIDs();

				if (query.isComputeSize())
					total = builder.selectCount();

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
