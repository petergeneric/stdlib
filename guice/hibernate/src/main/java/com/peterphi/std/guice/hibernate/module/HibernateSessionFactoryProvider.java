package com.peterphi.std.guice.hibernate.module;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;

/**
 * A SessionFactory Provider for Guice
 */
class HibernateSessionFactoryProvider implements Provider<SessionFactory>, StoppableService
{
	private static final Logger log = Logger.getLogger(HibernateSessionFactoryProvider.class);

	private final Configuration config;
	private SessionFactory sessionFactory;

	@Inject
	public HibernateSessionFactoryProvider(ShutdownManager coordinator, Configuration config)
	{
		coordinator.register(this);
		this.config = config;
	}

	public synchronized SessionFactory get()
	{
		if (this.sessionFactory == null)
		{
			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties())
			                                                              .buildServiceRegistry();
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
