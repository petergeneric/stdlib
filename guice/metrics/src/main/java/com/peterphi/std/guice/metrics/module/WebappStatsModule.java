package com.peterphi.std.guice.metrics.module;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;

public class WebappStatsModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		RestResourceRegistry.register(MetricsRestService.class);
	}
}
