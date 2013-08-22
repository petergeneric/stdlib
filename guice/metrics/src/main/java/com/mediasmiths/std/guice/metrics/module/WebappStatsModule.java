package com.mediasmiths.std.guice.metrics.module;

import com.google.inject.AbstractModule;
import com.mediasmiths.std.guice.metrics.rest.api.MetricsRestService;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class WebappStatsModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		RestResourceRegistry.register(MetricsRestService.class);
	}
}
