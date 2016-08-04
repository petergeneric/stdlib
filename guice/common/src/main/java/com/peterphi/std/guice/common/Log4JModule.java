package com.peterphi.std.guice.common;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.log4j.InstrumentedAppender;
import com.google.inject.AbstractModule;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Reads the <code>log4j.properties</code> value from the service config; if a value is supplied it searches the classpath for
 * this and loads it into log4j.<br />
 * If the log4j.properties property is not specified then the default behaviour is observed (log4j configures itself
 * automatically)
 */
public class Log4JModule extends AbstractModule
{
	private static Logger log = Logger.getLogger(Log4JModule.class);

	private GuiceConfig guiceConfig;
	private String configFile;
	private MetricRegistry registry;


	public Log4JModule(GuiceConfig configuration, MetricRegistry registry)
	{
		this.registry = registry;
		this.guiceConfig = configuration;
		configFile = configuration.get(GuiceProperties.LOG4J_PROPERTIES_FILE, null);
	}


	@Override
	protected void configure()
	{
		if (configFile != null)
		{
			final Properties config;
			{
				if (StringUtils.contains(configFile, '\n'))
				{
					log.debug("Assuming log4j.properties contains literal log4j.properties file, not a resource/file reference");
					config = PropertyFile.fromString(configFile, "log4j.inline").toProperties();
				}
				else if (configFile != null)
				{
					log.debug("Loading log4j configuration from " + configFile);

					if (configFile.equals("embedded"))
					{
						// Load the log4j config from the guice configuration
						config = guiceConfig.toProperties(key -> StringUtils.startsWithIgnoreCase(key, "log4j."));
					}
					else
					{
						// Load the log4j file directly
						PropertyFile props = PropertyFile.find(configFile);

						// Now resolve any ${} properties within the log4j file against the guice config
						GuiceConfig temp = new GuiceConfig();
						temp.setAll(guiceConfig);
						temp.setAll(props);

						// Finally, extract the original property values with their values resolved
						config = temp.toProperties(key -> props.keySet().contains(key));
					}
				}
				else
				{
					//wont actually happen but to guarrente there is a value for config later on
					throw new RuntimeException("Unexpected logging configuration");
				}
			}

			//reset any existing log config
			LogManager.resetConfiguration();

			//apply the specified properties
			PropertyConfigurator.configure(config);
		}
		else
		{
			log.debug("Leaving logging subsystem to initialise itself");
		}

		// Register a custom appender for metrics gathering
		InstrumentedAppender log4jmetrics = new InstrumentedAppender(registry);
		log4jmetrics.activateOptions();
		LogManager.getRootLogger().addAppender(log4jmetrics);
	}
}
