package com.peterphi.std.guice.hibernate.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.common.serviceprops.ConfigurationConverter;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.usertype.DateUserType;
import com.peterphi.std.guice.hibernate.usertype.JodaDateTimeUserType;
import com.peterphi.std.guice.hibernate.usertype.JodaLocalDateUserType;
import com.peterphi.std.guice.hibernate.usertype.SampleCountUserType;
import com.peterphi.std.guice.hibernate.usertype.TimecodeUserType;
import com.peterphi.std.io.PropertyFile;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.usertype.UserType;

import java.util.Properties;

public abstract class HibernateModule extends AbstractModule
{
	/**
	 * The property name in the service.properties file to read the location of hibernate.properties from
	 */
	private static final String PROPFILE_KEY = "hibernate.properties";

	private static final String PROPFILE_VAL_EMBEDDED = "embedded";


	@Override
	protected void configure()
	{
		bind(ServiceRegistry.class).toProvider(HibernateServiceRegistryProvider.class).in(Singleton.class);
		bind(SessionFactory.class).toProvider(HibernateSessionFactoryProvider.class).in(Singleton.class);

		bind(Session.class).toProvider(SessionProvider.class);
		bind(Transaction.class).toProvider(TransactionProvider.class);

		TransactionMethodInterceptor interceptor = new TransactionMethodInterceptor(getProvider(Session.class));

		// handles @Transactional methods
		binder().bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), interceptor);
	}


	@Provides
	@Singleton
	public Configuration getHibernateConfiguration(org.apache.commons.configuration.Configuration configuration,
	                                               @Doc("the source for hibernate.properties (either embedded or a filepath to search for using the classpath)")
	                                               @Named(PROPFILE_KEY) String propertyFileName)
	{
		final Properties properties;

		if (PROPFILE_VAL_EMBEDDED.equals(propertyFileName))
		{
			properties = ConfigurationConverter.toProperties(configuration);
		}
		else
		{
			PropertyFile[] files = PropertyFile.findAll(propertyFileName);

			if (files == null || files.length == 0)
			{
				throw new IllegalArgumentException("Cannot find any property files called: " + propertyFileName);
			}
			else
			{
				// Merge all the values and interpret them via commons-configuration to allow for interpolation
				properties = ConfigurationConverter.toProperties(ConfigurationConverter.union(files));
			}
		}

		org.hibernate.cfg.Configuration config = new org.hibernate.cfg.Configuration();
		config.addProperties(properties);

		configure(config);

		registerTypes(config);

		return config;
	}


	protected void registerTypes(final Configuration config)
	{
		// Map Date and DateTime types to BIGINT by default
		registerType(config, DateUserType.INSTANCE);
		registerType(config, JodaDateTimeUserType.INSTANCE);

		registerType(config, TimecodeUserType.INSTANCE);
		registerType(config, SampleCountUserType.INSTANCE);

		// Map LocalDate to DATE
		registerType(config, JodaLocalDateUserType.INSTANCE);
	}


	private void registerType(Configuration configuration, UserType type)
	{
		String className = type.returnedClass().getName();
		configuration.registerTypeOverride(type, new String[]{className});
	}


	/**
	 * Perform any steps necessary to fully configure the Hibernate Configuration provided<br />
	 * The Configuration will already have been pre-populated with the properties from hibernate.properties
	 *
	 * @param config
	 */
	protected abstract void configure(Configuration config);
}
