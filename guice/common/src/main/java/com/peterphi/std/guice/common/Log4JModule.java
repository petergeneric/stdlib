package com.peterphi.std.guice.common;

import com.google.inject.AbstractModule;
import com.peterphi.std.io.PropertyFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Reads the <code>log4j.properties</code> value from the service config; if a value is supplied it searches the classpath for
 * this and loads it into log4j.<br />
 * If the log4j.properties property is not specified then the default behaviour is observed (log4j configures itself
 * automatically)
 */
public class Log4JModule extends AbstractModule
{
	private static Logger log = Logger.getLogger(Log4JModule.class);

	private PropertyFile guiceConfig;
	private String configFile;


	public Log4JModule(PropertyFile properties)
	{
		this.guiceConfig = properties;
		configFile = properties.get("log4j.properties", null);
	}


	@Override
	protected void configure()
	{
		if (configFile != null)
		{
			log.debug("Loading log4j configuration from " + configFile);

			final PropertyFile config;

			if (configFile.equals("embedded"))
			{
				// Load the log4j config from the guice configuration
				config = guiceConfig;
			}
			else
			{
				// Load the log4j config file
				config = PropertyFile.find(configFile);
			}

			//reset any existing log config
			LogManager.resetConfiguration();

			//apply the specified properties
			PropertyConfigurator.configure(config.toProperties());
		}
		else
		{
			log.debug("Leaving logging subsystem to initialise itself");
		}
	}
}
