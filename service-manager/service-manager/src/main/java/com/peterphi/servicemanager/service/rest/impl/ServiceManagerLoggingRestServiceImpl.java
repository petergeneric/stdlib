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
import java.util.Arrays;
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
		// Convert the wire format to the storage format
		List<LogLineTableEntity> lines = convert(logs.getServiceId(), Arrays.asList(logs.getLines()));

		// Now store the log data
		service.store(lines);
	}


	private List<LogLineTableEntity> convert(final String serviceId, final List<LogLine> lines)
	{
		List<LogLineTableEntity> result = new ArrayList<>(lines.size());

		final ServiceInstanceEntity entity = cache.get(serviceId);

		for (LogLine line : lines)
		{
			result.add(new LogLineTableEntity(new DateTime(line.getWhen()),
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
			                                  line.getMessage()));
		}

		return result;
	}
}
