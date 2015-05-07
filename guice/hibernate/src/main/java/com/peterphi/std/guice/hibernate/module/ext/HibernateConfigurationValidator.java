package com.peterphi.std.guice.hibernate.module.ext;

import org.hibernate.cfg.Configuration;

import java.util.Properties;

/**
 * Service interface for plugins wishing to validate a Hibernate configuration before it is permitted to access the database
 */
public interface HibernateConfigurationValidator
{
	void validate(Configuration hibernateConfiguration,
	              Properties hibernateProperties,
	              org.apache.commons.configuration.Configuration environmentConfiguration);
}
