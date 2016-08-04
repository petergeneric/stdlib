package com.peterphi.std.guice.liquibase.hibernate;

import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.hibernate.module.ext.HibernateConfigurationValidator;
import com.peterphi.std.guice.liquibase.LiquibaseAction;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

/**
 * Hibernate Configuration Validator that makes sure database has all appropriate liquibase migrations applied before hibernate
 * is
 * permitted to access the database.
 */
public class LiquibaseHibernateValidator implements HibernateConfigurationValidator
{
	private static final Logger log = Logger.getLogger(LiquibaseHibernateValidator.class);


	@Override
	public void validate(final Configuration hibernateConfiguration,
	                     final Properties hibernateProperties,
	                     final GuiceConfig environmentConfiguration)
	{
		// Figure out which liquibase action we should perform
		final String str = hibernateProperties.getProperty(GuiceProperties.LIQUIBASE_ACTION,
		                                                   environmentConfiguration.get(GuiceProperties.LIQUIBASE_ACTION,
		                                                                                      "ASSERT_UPDATED"));

		final LiquibaseAction action = LiquibaseAction.valueOf(str);

		// Execute the appropriate action
		LiquibaseCore.execute(environmentConfiguration, hibernateProperties, action);
	}
}
