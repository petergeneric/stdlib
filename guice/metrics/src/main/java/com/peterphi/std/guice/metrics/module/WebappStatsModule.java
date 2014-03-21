package com.peterphi.std.guice.metrics.module;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import org.apache.log4j.Logger;


public class WebappStatsModule extends AbstractModule
{

	private final static Logger log = Logger.getLogger(WebappStatsModule.class);

	@Override
	protected void configure()
	{
		RestResourceRegistry.register(MetricsRestService.class);
	}

	@Provides
	public HealthCheckRegistry produceHealthCheckRegistry() {
		log.info("Starting HealthCheckRegistry");
		HealthCheckRegistry healthChecks = new HealthCheckRegistry();
		return healthChecks;
	}

}
