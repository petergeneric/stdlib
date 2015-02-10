package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.shutdown.iface.ShutdownManager;
import com.peterphi.std.guice.common.shutdown.iface.StoppableService;
import com.peterphi.std.guice.database.annotation.Transactional;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import javax.inject.Named;

/**
 * A SessionFactory Provider for Guice
 */
class HibernateSessionFactoryProvider implements Provider<SessionFactory>, StoppableService
{
	private static final Logger log = Logger.getLogger(HibernateSessionFactoryProvider.class);

	private final Provider<ServiceRegistry> serviceRegistryProvider;
	private final Configuration config;

	private SessionFactory sessionFactory;

	@Inject(optional = true)
	@Named("hibernate.shutdown.sql")
	@Doc("The SQL to run on the database before a shutdown occurs")
	private String shutdownSql;


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
			runShutDownSQL();
			sessionFactory.close();
			sessionFactory = null;
		}
		else
		{
			log.info("SessionFactory already null");
		}
	}


	@Transactional
	void runShutDownSQL()
	{
		if (StringUtils.isNotEmpty(shutdownSql))
		{
			sessionFactory.getCurrentSession().createSQLQuery(shutdownSql).executeUpdate();
		}
	}
}
