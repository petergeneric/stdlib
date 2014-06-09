package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

class HibernateServiceRegistryProvider implements Provider<ServiceRegistry>
{
	private final Configuration config;
	private ServiceRegistry registry;


	@Inject
	public HibernateServiceRegistryProvider(Configuration config)
	{
		this.config = config;
	}


	public synchronized ServiceRegistry get()
	{
		if (this.registry == null)
		{
			this.registry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
		}

		return this.registry;
	}
}
