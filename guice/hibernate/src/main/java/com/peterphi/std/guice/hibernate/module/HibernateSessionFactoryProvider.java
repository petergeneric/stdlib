package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * A SessionFactory Provider for Guice
 */
class HibernateSessionFactoryProvider implements Provider<SessionFactory>, StoppableService
{
	private static final Logger log = Logger.getLogger(HibernateSessionFactoryProvider.class);

	private final Provider<ServiceRegistry> serviceRegistryProvider;
	private final Configuration config;

	private SessionFactory sessionFactory;


	@Inject
	public HibernateSessionFactoryProvider(ShutdownManager coordinator,
	                                       Provider<ServiceRegistry> serviceRegistryProvider,
	                                       Configuration config)
	{
		this.serviceRegistryProvider = serviceRegistryProvider;
		this.config = config;

		coordinator.register(this);
	}


	public synchronized SessionFactory get()
	{
		if (this.sessionFactory == null)
		{
			final ServiceRegistry serviceRegistry = serviceRegistryProvider.get();

			this.sessionFactory = config.buildSessionFactory(serviceRegistry);
			log.trace("Hibernate Setup Complete.");
		}

		return sessionFactory;
	}


	@Override
	public void shutdown()
	{
		log.info("Shut down Hibernate SessionFactory...");

		if (sessionFactory != null)
		{
			sessionFactory.close();
			sessionFactory = null;
		}
	}
}
