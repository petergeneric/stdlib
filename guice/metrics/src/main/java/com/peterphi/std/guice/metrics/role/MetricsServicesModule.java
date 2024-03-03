package com.peterphi.std.guice.metrics.role;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.metrics.rest.api.MetricsRestService;
import com.peterphi.std.guice.metrics.rest.impl.MetricsRestServiceImpl;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;

public class MetricsServicesModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		RestResourceRegistry.register(MetricsRestService.class);
		bind(MetricsRestService.class).to(MetricsRestServiceImpl.class);
	}
}
