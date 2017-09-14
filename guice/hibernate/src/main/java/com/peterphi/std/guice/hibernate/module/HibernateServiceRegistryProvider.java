package com.peterphi.std.guice.hibernate.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

class HibernateServiceRegistryProvider implements Provider<ServiceRegistry>
{
	private final Configuration config;
	private StandardServiceRegistry registry;


	@Inject
	public HibernateServiceRegistryProvider(Configuration config)
	{
		this.config = config;
	}


	public synchronized StandardServiceRegistry get()
	{
		if (this.registry == null)
		{
			this.registry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
		}

		return this.registry;
	}
}
