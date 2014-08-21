package com.peterphi.std.guice.metrics.module;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.peterphi.std.guice.metrics.rest.api.HealthRestService;
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
		RestResourceRegistry.register(HealthRestService.class);
	}

	@Provides
	@Singleton
	public HealthCheckRegistry produceHealthCheckRegistry() {
		log.info("Starting HealthCheckRegistry");
		return new HealthCheckRegistry();
	}

}
