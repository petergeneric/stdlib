package com.peterphi.std.guice.hibernate.role;

import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import org.hibernate.cfg.Configuration;

import javax.persistence.Entity;

class AutoHibernateModule extends HibernateModule
{
	private final ClassScanner scanner;


	public AutoHibernateModule(final ClassScanner scanner, final MetricRegistry metrics)
	{
		super(metrics);

		this.scanner = scanner;
	}


	@Override
	protected void configure(final Configuration config)
	{
		for (Class<?> clazz : scanner.getAnnotatedClasses(Entity.class))
			config.addAnnotatedClass(clazz);
	}
}
