package com.peterphi.std.guice.liquibase.hibernate;

import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.ConfigurationValueProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AvailableSettings;

import javax.naming.InitialContext;
import java.util.Properties;

class GuiceApplicationValueContainer implements ConfigurationValueProvider
{
	private static final Logger log = Logger.getLogger(GuiceApplicationValueContainer.class);

	private static final String LIQUIBASE_SCHEMA_DEFAULT = "liquibase.schema.default";
	private static final String HIBERNATE_SCHEMA_DEFAULT = AvailableSettings.DEFAULT_SCHEMA;

	private static final String LIQUIBASE_DATASOURCE = "liquibase.datasource";
	private static final String HIBERNATE_DATASOURCE = AvailableSettings.DATASOURCE;


	private final GuiceConfig applicationConfiguration;
	private final InitialContext initialContext;
	private final Properties hibernateConfiguration;


	public GuiceApplicationValueContainer(final GuiceConfig applicationConfiguration,
	                                      final InitialContext initialContext,
	                                      final Properties hibernateConfiguration)
	{
		this.applicationConfiguration = applicationConfiguration;
		this.initialContext = initialContext;
		this.hibernateConfiguration = hibernateConfiguration;
	}


	@Override
	public String describeValueLookupLogic(ConfigurationProperty property)
	{
		return "JNDI/application/hibernate/system property '" + property.getNamespace() + "." + property.getName() + "'";
	}


	@Override
	public Object getValue(String namespace, String property)
	{
		return getValue(namespace + "." + property);
	}


	/**
	 * Try to read the value that is stored by the given key from
	 * <ul>
	 * <li>the guice application's configuration properties</li>
	 * <li>the guice application's hibernate properties</li>
	 * <li>system properties</li>
	 * </ul>
	 */
	public String getValue(String key)
	{
		return StringUtils.trimToNull(getRawValue(key));
	}


	private String getRawValue(String key)
	{
		if (log.isTraceEnabled())
			log.trace("getProperty: " + key);

		// Load from the guice environment configuration
		if (applicationConfiguration.containsKey(key))
		{
			return applicationConfiguration.get(key);
		}

		// Fall back on hibernate configuration
		if (hibernateConfiguration != null && hibernateConfiguration.containsKey(key))
		{
			return hibernateConfiguration.getProperty(key);
		}

		// Finally, fall back on loading a system property
		return System.getProperty(key);
	}


	public String getDataSource()
	{
		final String liquibaseValue = getValue(LIQUIBASE_DATASOURCE);

		if (StringUtils.isEmpty(liquibaseValue))
		{
			return getValue(HIBERNATE_DATASOURCE);
		}
		else
		{
			return liquibaseValue;
		}
	}


	public String getDefaultSchema()
	{
		final String liquibaseValue = getValue(LIQUIBASE_SCHEMA_DEFAULT);

		if (StringUtils.isEmpty(liquibaseValue))
		{
			return getValue(HIBERNATE_SCHEMA_DEFAULT);
		}
		else
		{
			return liquibaseValue;
		}
	}
}
