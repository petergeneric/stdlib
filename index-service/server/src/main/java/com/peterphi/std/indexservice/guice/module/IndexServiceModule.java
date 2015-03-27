package com.peterphi.std.indexservice.guice.module;

import com.google.inject.AbstractModule;
import com.peterphi.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.peterphi.std.indexservice.rest.iface.IndexRestService;
import com.peterphi.std.indexservice.rest.impl.IndexRestServiceImpl;
import com.peterphi.std.indexservice.rest.impl.repo.ExpireApplicationsWorker;

public class IndexServiceModule extends AbstractModule
{

	@Override
	protected void configure()
	{

	}

}