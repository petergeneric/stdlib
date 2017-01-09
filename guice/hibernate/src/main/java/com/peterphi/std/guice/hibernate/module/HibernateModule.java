package com.peterphi.std.guice.hibernate.module;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.hibernate.module.ext.HibernateConfigurationValidator;
import com.peterphi.std.guice.hibernate.module.logging.HibernateObservingInterceptor;
import com.peterphi.std.guice.hibernate.usertype.DateUserType;
import com.peterphi.std.guice.hibernate.usertype.JodaDateTimeUserType;
import com.peterphi.std.guice.hibernate.usertype.JodaLocalDateUserType;
import com.peterphi.std.guice.hibernate.usertype.SampleCountUserType;
import com.peterphi.std.guice.hibernate.usertype.TimecodeUserType;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.usertype.UserType;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

public abstract class HibernateModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(HibernateModule.class);

	/**
	 * If hibernate.properties is set to this value, hibernate properties are assumed to be embedded in app config
	 */
	private static final String PROPFILE_VAL_EMBEDDED = "embedded";


	public HibernateModule(final MetricRegistry registry)
	{
		this.registry = registry;
	}


	private final MetricRegistry registry;


	@Override
	protected void configure()
	{
		bind(ServiceRegistry.class).toProvider(HibernateServiceRegistryProvider.class).in(Singleton.class);
		bind(SessionFactory.class).toProvider(HibernateSessionFactoryProvider.class).in(Singleton.class);

		bind(Session.class).toProvider(SessionProvider.class);
		bind(Transaction.class).toProvider(TransactionProvider.class);

		TransactionMethodInterceptor txinterceptor = new TransactionMethodInterceptor(getProvider(Session.class), registry);

		// handles @Transactional methods
		binder().bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), txinterceptor);
	}


	@Provides
	@Singleton
	public Configuration getHibernateConfiguration(GuiceConfig guiceConfig,
	                                               @Named(GuiceProperties.HIBERNATE_PROPERTIES) String propertyFileName,
	                                               HibernateObservingInterceptor interceptor)
	{
		final Properties properties = extractHibernateProperties(guiceConfig, propertyFileName);

		validateHibernateProperties(guiceConfig, properties);

		// Set up the hibernate Configuration
		Configuration config = new Configuration();

		// Set up the interceptor
		config.setInterceptor(interceptor.getInterceptor());

		config.addProperties(properties);

		configure(config);

		registerTypes(config);

		{
			ServiceLoader<HibernateConfigurationValidator> services = ServiceLoader.load(HibernateConfigurationValidator.class);

			final Iterator<HibernateConfigurationValidator> it = services.iterator();

			if (log.isTraceEnabled())
				log.trace("Evaluate HibernateConfigurationValidators. has at least one=" + it.hasNext());

			while (it.hasNext())
			{
				final HibernateConfigurationValidator validator = it.next();

				if (log.isTraceEnabled())
					log.trace("Validating hibernate configuration with " + validator);

				// Have the validator check the hibernate/database configuration
				validator.validate(config, properties, guiceConfig);
			}
		}

		return config;
	}


	private Properties extractHibernateProperties(final GuiceConfig guiceConfig,
	                                              final @Named(GuiceProperties.HIBERNATE_PROPERTIES) String propertyFileName)
	{
		final Properties properties;

		if (PROPFILE_VAL_EMBEDDED.equals(propertyFileName))
		{
			// Extract all properties starting with "hibernate." and "liquibase."
			properties = guiceConfig.toProperties(k -> k.startsWith("hibernate.") || k.startsWith("liquibase."));
		}
		else
		{
			if (StringUtils.contains(propertyFileName, '\n'))
			{
				log.debug(
						"Assuming hibernate.properties contains literal hibernate.properties file, not a resource/file reference");
				properties = PropertyFile.fromString(propertyFileName).toProperties();
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
					// Merge all hibernate property files into a single file
					PropertyFile file = PropertyFile.readOnlyUnion(files);

					// Now Merge all the values and interpret them via the guice config to allow for interpolation of variables
					GuiceConfig temp = new GuiceConfig();

					temp.setAll(guiceConfig);
					temp.setAll(file);

					// Now extract the hibernate properties again with any variables
					properties = temp.toProperties(key -> file.containsKey(key));
				}
			}
		}
		return properties;
	}


	/**
	 * Checks whether hbm2ddl is set to a prohibited value, throwing an exception if it is
	 *
	 * @param configuration
	 * 		the global app config
	 * @param hibernateProperties
	 * 		the hibernate-specific config
	 *
	 * @throws IllegalArgumentException
	 * 		if the hibernate.hbm2ddl.auto property is set to a prohibited value
	 */
	private void validateHibernateProperties(final GuiceConfig configuration, final Properties hibernateProperties)
	{
		final boolean allowCreateSchema = configuration.getBoolean(GuiceProperties.HIBERNATE_ALLOW_HBM2DDL_CREATE, false);

		if (!allowCreateSchema)
		{
			// Check that hbm2ddl is not set to a prohibited value, throwing an exception if it is
			final String hbm2ddl = hibernateProperties.getProperty("hibernate.hbm2ddl.auto");

			if (hbm2ddl != null && (hbm2ddl.equalsIgnoreCase("create") || hbm2ddl.equalsIgnoreCase("create-drop")))
			{
				throw new IllegalArgumentException("Value '" +
				                                   hbm2ddl +
				                                   "' is not permitted for hibernate property 'hibernate.hbm2ddl.auto' under the current configuration, consider using 'update' instead. If you must use the value 'create' then set configuration parameter '" +
				                                   GuiceProperties.HIBERNATE_ALLOW_HBM2DDL_CREATE +
				                                   "' to 'true'");
			}
		}
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
