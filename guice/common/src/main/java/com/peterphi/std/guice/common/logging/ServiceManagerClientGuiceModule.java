package com.peterphi.std.guice.common.logging;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.logging.appender.ServiceManagerLogForwardDaemon;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerLoggingRestService;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerRegistryRestService;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.restclient.JAXRSProxyClientFactory;

public class ServiceManagerClientGuiceModule extends AbstractModule
{
	private LogForwardDaemonPreGuice startup;


	public ServiceManagerClientGuiceModule(GuiceConfig config, ShutdownManager shutdown)
	{
		this.startup = new LogForwardDaemonPreGuice(shutdown, config);
	}


	@Override
	protected void configure()
	{
		// replace the
		requestInjection(this.startup.getDaemon());

		bind(ServiceManagerLogForwardDaemon.class).toInstance(this.startup.getDaemon());
	}


	@Provides
	@Singleton
	public ServiceManagerLoggingRestService getLoggingRestService(JAXRSProxyClientFactory proxyFactory)
	{
		return proxyFactory.getClient(ServiceManagerLoggingRestService.class);
	}


	@Provides
	@Singleton
	public ServiceManagerRegistryRestService getRegistryRestService(JAXRSProxyClientFactory proxyFactory)
	{
		return proxyFactory.getClient(ServiceManagerRegistryRestService.class);
	}
}
