package com.peterphi.std.guice.hibernate.role;

import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

class AutoHibernateModule extends HibernateModule
{
	private static final Logger log = Logger.getLogger(AutoHibernateModule.class);

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
			if (!clazz.isAnnotationPresent(MappedSuperclass.class))
			{
				log.trace("Registering @Entity class with hibernate: " + clazz.getName());
				config.addAnnotatedClass(clazz);
			}
			else
			{
				log.debug("Ignoring @Entity class because it has @MappedSuperclass: " + clazz.getName());
			}
	}
}
