package com.peterphi.std.guice.common.logging;

import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.logging.appender.ServiceManagerLogForwardDaemon;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerLoggingRestService;
import com.peterphi.std.guice.common.logging.rest.iface.ServiceManagerRegistryRestService;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.restclient.resteasy.impl.JAXBContextResolver;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyClientFactoryImpl;
import com.peterphi.std.guice.restclient.resteasy.impl.ResteasyProxyClientFactoryImpl;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.apache.log4j.Logger;

import java.net.URI;

/**
 * Constructs the {@link ServiceManagerLogForwardDaemon} before the Guice environment is fully setup (so that logs can be
 * forwarded while guice is being set up - and so guice setup failures can be logged remotely)
 */
class LogForwardDaemonPreGuice implements StoppableService
{
	private static final Logger log = Logger.getLogger(LogForwardDaemonPreGuice.class);

	private ServiceManagerLogForwardDaemon daemon;
	private GuiceConfig config;

	private ResteasyClientFactoryImpl clientFactory;


	public LogForwardDaemonPreGuice(ShutdownManager shutdownManager, GuiceConfig config)
	{
		this.config = config;

		// Make sure we can safely clean up if the guice environment construction fails
		shutdownManager.register(this);

		start();
	}


	private void start()
	{
		final String instanceId = config.get(GuiceProperties.INSTANCE_ID);
		final String localEndpoint = config.get(GuiceProperties.LOCAL_REST_SERVICES_ENDPOINT, "http://unknown"); // TODO fix this!
		final boolean useMoxy = config.getBoolean(GuiceProperties.MOXY_ENABLED, true);

		// Construct the necessary classes to create resteasy proxy clients.
		// We must do this independent of the guice creation path because we'll be running as Guice is starting up
		final JAXBContextResolver jaxb = new JAXBContextResolver(new JAXBSerialiserFactory(useMoxy));
		this.clientFactory = new ResteasyClientFactoryImpl(null, null, jaxb);
		ResteasyProxyClientFactoryImpl proxyFactory = new ResteasyProxyClientFactoryImpl(clientFactory, config);

		// Instantiate the services
		final ServiceManagerLoggingRestService logService = proxyFactory.getClient(ServiceManagerLoggingRestService.class);
		final ServiceManagerRegistryRestService registryService = proxyFactory.getClient(ServiceManagerRegistryRestService.class);

		this.daemon = new ServiceManagerLogForwardDaemon(instanceId,
		                                                 URI.create(localEndpoint),
		                                                 registryService,
		                                                 logService,
		                                                 this :: guiceHasTakenOver);

		// Start the thread running
		this.daemon.startThread();
	}


	@Override
	public void shutdown()
	{
		if (daemon != null)
		{
			try
			{
				daemon.shutdown();
			}
			catch (Throwable t)
			{
				log.info("Failed to shut down log forwarder daemon", t);
			}
		}

		shutdownClientFactory();
	}


	public void shutdownClientFactory()
	{
		if (clientFactory != null)
		{
			clientFactory.shutdown();
			clientFactory = null;
		}
	}


	public ServiceManagerLogForwardDaemon getDaemon()
	{
		return daemon;
	}


	/**
	 * Called when the proper Guice environment has taken over the Daemon. This signals that the JAX-RS clients we created for the
	 * pre-guice stage are no longer necessary and the HttpClient constructed for their use can be discarded.
	 */
	public void guiceHasTakenOver()
	{
		shutdownClientFactory();

		// This object will be retained (because it's registered with the Shutdown Manager) so let's clear out all references
		// because they are no longer needed by us
		this.daemon = null;
		this.config = null;
		this.clientFactory = null;
	}
}
