package com.mediasmiths.std.indexservice.guice.module;

import com.google.inject.AbstractModule;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.indexservice.rest.iface.IndexRestService;
import com.mediasmiths.std.indexservice.rest.impl.IndexRestServiceImpl;
import com.mediasmiths.std.indexservice.rest.impl.repo.ExpireApplicationsWorker;

public class IndexServiceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(IndexRestService.class).to(IndexRestServiceImpl.class);

		RestResourceRegistry.register(IndexRestService.class);

		bind(ExpireApplicationsWorker.class).asEagerSingleton();
	}

}
