package com.peterphi.std.guice.hibernate.role;

import com.codahale.metrics.MetricRegistry;
import com.peterphi.std.guice.common.ClassScanner;
import com.peterphi.std.guice.common.ClassScannerFactory;
import com.peterphi.std.guice.hibernate.module.HibernateModule;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;

import javax.persistence.Entity;

class AutoHibernateModule extends HibernateModule
{
	private static final Logger log = Logger.getLogger(AutoHibernateModule.class);

	private final ClassScannerFactory scannerFactory;


	public AutoHibernateModule(final ClassScannerFactory scannerFactory, final MetricRegistry metrics)
	{
		super(metrics);

		this.scannerFactory = scannerFactory;
	}


	@Override
	protected void configure(final Configuration config)
	{
		final ClassScanner scanner = scannerFactory.getInstance();

		if (scanner == null)
			throw new IllegalArgumentException("No classpath scanner available, missing scan.packages?");

		for (Class<?> clazz : scanner.getAnnotatedClasses(Entity.class))
		{
			log.trace("Registering @Entity class with hibernate: " + clazz.getName());
			config.addAnnotatedClass(clazz);
		}
	}
}
