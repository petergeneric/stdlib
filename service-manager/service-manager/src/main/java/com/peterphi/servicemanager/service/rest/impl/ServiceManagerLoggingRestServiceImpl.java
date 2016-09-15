package com.peterphi.servicemanager.service.rest.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.peterphi.servicemanager.service.db.entity.ServiceInstanceEntity;
import com.peterphi.servicemanager.service.logging.LogLineTableEntity;
import com.peterphi.servicemanager.service.logging.hub.LoggingService;
import com.peterphi.std.guice.common.logging.logreport.LogLine;
import com.peterphi.std.guice.common.logging.logreport.LogReport;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerLoggingRestService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ServiceManagerLoggingRestServiceImpl implements ServiceManagerLoggingRestService
{
	private static final Logger log = Logger.getLogger(ServiceManagerLoggingRestServiceImpl.class);

	@Inject
	LoggingService service;

	@Inject
	ServiceIdCache cache;


	@Override
	public void report(final LogReport logs)
	{
		if (log.isTraceEnabled())
			log.trace("Received " + logs.getLines().length + " log lines to store");

		try
		{
			final ServiceInstanceEntity serviceInstance = cache.get(logs.getServiceId());

			// Make sure that all store calls are for the same partition (date + instance id)
			// This is technically only a requirement for Azure but it's a guarantee other
			// stores can use effectively (e.g. writing to datestamped log files)
			String partitionKey = null;
			List<LogLineTableEntity> pending = new ArrayList<>();
			for (LogLine line : logs.getLines())
			{
				LogLineTableEntity entity = convert(serviceInstance, line);

				if (partitionKey == null)
				{
					// First entry in a new partition
					partitionKey = entity.getPartitionKey();
				}
				else if (!partitionKey.equals(entity.getPartitionKey()))
				{
					// Flush all the lines up til now and then start a new list
					service.store(pending);

					pending = new ArrayList<>();
					partitionKey = entity.getPartitionKey();
				}

				pending.add(entity);
			}

			// Make sure we flush any remaining data to the storage system
			service.store(pending);
		}
		catch (Throwable t)
		{
			log.error("Error saving logs", t);
			throw t;
		}
	}


	private LogLineTableEntity convert(final ServiceInstanceEntity entity, final LogLine line)
	{
		return new LogLineTableEntity(new DateTime(line.getWhen()),
		                              line.getCategory(),
		                              line.getLevel(),
		                              entity.getEndpoint(),
		                              entity.getId(),
		                              entity.getCodeRevision(),
		                              line.getThread(),
		                              line.getUserId(),
		                              line.getTraceId(),
		                              line.getExceptionId(),
		                              line.getException(),
		                              line.getMessage());
	}
}
