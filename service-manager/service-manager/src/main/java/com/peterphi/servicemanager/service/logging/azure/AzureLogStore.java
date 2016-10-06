package com.peterphi.servicemanager.service.logging.azure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.peterphi.servicemanager.service.guice.ServiceManagerGuiceModule;
import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import com.peterphi.servicemanager.service.logging.LogStore;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.retry.annotation.Retry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class AzureLogStore implements LogStore
{
	private static final Logger log = Logger.getLogger(AzureLogStore.class);

	private static final int BATCH_SIZE = 100;

	@Inject
	@Named("logdata")
	CloudTable logdata;

	/**
	 * N.B. included here so it can be discovered, used in {@link ServiceManagerGuiceModule#getLogDataTable}
	 */
	@Inject
	@Doc("The azure table to use for storing log lines (if azure is enabled)")
	@Named("azure.logging-table")
	public String logTableName;


	@Override
	public void store(List<LogLineTableEntity> lines)
	{
		if (lines.size() <= BATCH_SIZE)
		{
			try
			{
				storeAsBatch(lines);
			}
			catch (StorageException e)
			{
				throw new RuntimeException("Error saving incoming lines in single batch", e);
			}
		}
		else
		{
			for (int start = 0; start < lines.size(); start += BATCH_SIZE)
			{
				try
				{
					List<LogLineTableEntity> batch = lines.subList(start, start + BATCH_SIZE);

					storeAsBatch(batch);
				}
				catch (StorageException e)
				{
					throw new RuntimeException("Error saving chunk of incoming lines, starting at index=" +
					                           start +
					                           ". Lines up until that index have been stored", e);
				}
			}
		}
	}


	@Override
	public boolean isSearchSupported()
	{
		return true;
	}


	@Retry
	public void storeAsBatch(final List<LogLineTableEntity> lines) throws StorageException
	{
		if (lines.isEmpty())
			return; // nothing to do!

		TableBatchOperation operation = new TableBatchOperation();

		for (LogLineTableEntity line : lines)
		{
			if (log.isTraceEnabled())
				log.trace("Storing entity with partition=" + line.getPartitionKey() + ", row=" + line.getRowKey());

			operation.insertOrReplace(line);
		}

		logdata.execute(operation);
	}


	public String createFilter(final Integer minLevel, final String traceId, final String exceptionId)
	{
		final List<String> filters = new ArrayList<>();

		if (minLevel != null)
			filters.add(TableQuery.generateFilterCondition("level", TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL, minLevel));

		if (traceId != null)
		{
			filters.add(TableQuery.generateFilterCondition("traceId",
			                                               TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
			                                               traceId));
			filters.add(TableQuery.generateFilterCondition("traceId", TableQuery.QueryComparisons.LESS_THAN, increment(traceId)));
		}

		if (exceptionId != null)
			filters.add(TableQuery.generateFilterCondition("exception-id", TableQuery.QueryComparisons.EQUAL, exceptionId));

		return andFilters(filters);
	}


	static String increment(final String traceId)
	{
		char lastChar = traceId.charAt(traceId.length() - 1);

		lastChar++;

		return StringUtils.chop(traceId) + Character.toString(lastChar);
	}


	private String andFilters(final String... filters)
	{
		return andFilters(Arrays.asList(filters));
	}


	private static String andFilters(List<String> filters)
	{
		if (filters.isEmpty() || filters.stream().filter(StringUtils:: isNotBlank).findAny().isPresent() == false)
		{
			return null;
		}
		else
		{
			// More than 1 filter, join them together
			return filters.stream().filter(StringUtils:: isNotBlank).collect(Collectors.joining(" and "));
		}
	}


	@Override
	public List<LogLineTableEntity> search(final DateTime from, DateTime to, final String filter)
	{
		return search(from, to, filter, Integer.MAX_VALUE);
	}


	public List<LogLineTableEntity> search(final DateTime from, DateTime to, final String filter, final long limit)
	{
		final String partitionAndRowQuery = createPartitionAndRowQuery(from, to);

		final String logLineFilter = andFilters(partitionAndRowQuery, filter);

		if (log.isTraceEnabled())
			log.trace("Search logs: " + logLineFilter);

		final Iterable<LogLineTableEntity> resultset = logdata.execute(TableQuery.from(LogLineTableEntity.class)
		                                                                         .where(logLineFilter));

		// Fetch back the requested number of results
		// N.B. we need to sort them according to when they were emitted because in Azure they're broken up
		// by service by time
		List<LogLineTableEntity> results = StreamSupport.stream(resultset.spliterator(), false)
		                                                .limit(limit)
		                                                .sorted(Comparator.comparing(LogLineTableEntity:: getDateTimeWhen))
		                                                .collect(Collectors.toList());

		return results;
	}


	/**
	 * Create a filter condition that returns log lines between two dates. Designed to be used in addition to other criteria
	 *
	 * @param from
	 * @param to
	 *
	 * @return
	 */
	public String createPartitionAndRowQuery(final DateTime from, final DateTime to)
	{
		final String parMin = TableQuery.generateFilterCondition("PartitionKey",
		                                                         TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
		                                                         from.toLocalDate().toString());

		final String rowMin = TableQuery.generateFilterCondition("RowKey",
		                                                         TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL,
		                                                         LogLineTableEntity.toRowKey(from, null));

		if (to == null || from.toLocalDate().equals(to.toLocalDate()) || to.isAfterNow())
		{
			// There's no upper bound (or the upper bound doesn't make sense to include in the query)
			return andFilters(parMin, rowMin);
		}
		else
		{
			// From and To required

			final String parMax = TableQuery.generateFilterCondition("PartitionKey",
			                                                         TableQuery.QueryComparisons.LESS_THAN,
			                                                         to.toLocalDate().plusDays(1).toString());

			final String rowMax = TableQuery.generateFilterCondition("RowKey",
			                                                         TableQuery.QueryComparisons.LESS_THAN,
			                                                         LogLineTableEntity.toRowKey(to.plusSeconds(1), null));

			return andFilters(parMin, parMax, rowMin, rowMax);
		}
	}
}
