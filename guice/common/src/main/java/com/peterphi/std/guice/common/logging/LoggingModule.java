package com.peterphi.std.guice.common.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.AbstractModule;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfigChangeObserver;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Reads the <code>log4j.properties</code> value from the service config; if a value is supplied it searches the classpath for
 * this and loads it into log4j.<br /> If the log4j.properties property is not specified then the default behaviour is observed
 * (log4j configures itself automatically)
 */
public class LoggingModule extends AbstractModule implements GuiceConfigChangeObserver
{
	private static final Logger log = Logger.getLogger(LoggingModule.class);

	public static final String DEFAULT_OUTPUT_PATTERN = "%d{ISO8601} %5p %m%n";

	private static boolean allowAutoReconfigure = true;

	private GuiceConfig guiceConfig;


	public LoggingModule(GuiceConfig configuration)
	{
		this.guiceConfig = configuration;

		configuration.registerChangeObserver(this);
	}


	@Override
	protected void configure()
	{
		reconfigure(guiceConfig, true);
	}


	public static void manualReconfigure(final GuiceConfig guice)
	{
		log.info("Manual reconfiguration of log4j.properties - disabling automatic reload!");
		allowAutoReconfigure = false;
		reconfigure(guice, true);
	}


	public static void autoReconfigure(final GuiceConfig guice)
	{
		if (allowAutoReconfigure)
			reconfigure(guice, true);
	}


	public static void setAllowAutoReconfigure(final boolean value)
	{
		allowAutoReconfigure = value;
	}


	public static boolean getAllowAutoReconfigure()
	{
		return allowAutoReconfigure;
	}


	/**
	 * Attempt to configure the logging system before the environment is fully prepared<br /> Will be subsequently reconfigured by
	 * the LoggingModule being constructed
	 *
	 * @param config
	 */
	public static void preconfigure(final GuiceConfig config)
	{
		reconfigure(config, false);
	}


	private static void reconfigure(final GuiceConfig config, boolean warnIfUsingDefault)
	{
		final String logbackConfigXml = readLogbackConfig(config);

		if (logbackConfigXml != null)
		{
			applyLogbackConfig(logbackConfigXml);
		}
		else
		{
			// Apply the default config
			applyLogbackConfig(Log4jToLogbackConverter.convert(new PropertyFile()));

			if (warnIfUsingDefault)
				log.warn("Using default logback at default configuration!");
		}
	}


	public static String readLogbackConfig(final GuiceConfig config)
	{
		try
		{
			final String logbackConfig = config.get(GuiceProperties.LOGBACK_CONFIG_FILE, null);

			// No logback config - fall back on translating log4j
			if (logbackConfig == null)
			{
				return tryLog4jPropertiesFallback(config);
			}
			else if (StringUtils.startsWith(logbackConfig, "<"))
			{
				// Inline XML
				log.trace("Assuming logback.xml contains literal logback XML file, not a resource/file reference");

				return logbackConfig;
			}
			else
			{
				try (final InputStream is = LoggingModule.class.getResourceAsStream(logbackConfig))
				{
					if (is != null)
					{
						return config.resolveVariables(IOUtils.toString(is));
					}
					else if (isLocalFile(logbackConfig))
					{
						// If this is a local file instead of a resource then load that
						return config.resolveVariables(FileUtils.readFileToString(new File(logbackConfig)));
					}
					else
					{
						log.warn("Invalid logback configuration file provided, using defaults. Requested config was: " +
						         logbackConfig);

						return null;
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Error initialising logback: " + e.getMessage(), e);

			return null;
		}
	}


	private static boolean isLocalFile(final String value)
	{
		if (value.length() > 2000)
			return false;

		final File f = new File(value);

		return (f.isAbsolute() && f.exists() && f.isFile());
	}


	private static void applyLogbackConfig(String xml)
	{
		// assume SLF4J is bound to logback in the current environment
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try
		{
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default
			// configuration. For multi-step configuration, omit calling context.reset().
			context.reset();

			configurator.doConfigure(new InputSource(new StringReader(xml)));
		}
		catch (Exception je)
		{
			// Print the error
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);

			// Now fail outright
			throw new RuntimeException(je);
		}
	}


	private static String tryLog4jPropertiesFallback(final GuiceConfig config) throws IOException
	{
		PropertyFile log4jConfig = getProperties(config);

		if (log4jConfig != null)
		{
			final String logbackConfig = Log4jToLogbackConverter.convert(log4jConfig);

			return logbackConfig;
		}

		return null; // No log4j configuration to convert
	}


	public static PropertyFile getProperties(final GuiceConfig guice)
	{
		final String log4jProperties = guice.get(GuiceProperties.LOG4J_PROPERTIES_FILE, null);

		if (log4jProperties == null)
			return null; // No log4j set up!

		if (StringUtils.contains(log4jProperties, '\n'))
		{
			log.debug("Assuming log4j.properties contains literal log4j.properties file, not a resource/file reference");
			return PropertyFile.fromString(log4jProperties, "log4j.inline");
		}
		else if (log4jProperties != null)
		{
			log.debug("Loading log4j configuration from " + log4jProperties);

			if (log4jProperties.equals("embedded"))
			{
				// Load the log4j config from the guice configuration
				return guice.toPropertyFile(key -> StringUtils.startsWithIgnoreCase(key, "log4j."));
			}
			else
			{
				// Load the log4j file directly
				PropertyFile props = PropertyFile.find(log4jProperties);

				// Now resolve any ${} properties within the log4j file against the guice config
				GuiceConfig temp = new GuiceConfig();
				temp.setAll(guice);
				temp.setAll(props);

				// Finally, extract the original property values with their values resolved
				return temp.toPropertyFile(key -> props.keySet().contains(key));
			}
		}
		else
		{
			// Won't actually happen but to guarantee there is a value for config later on
			throw new RuntimeException("log4j configuration is null!");
		}
	}


	@Override
	public void propertyChanged(final String name)
	{
		if (allowAutoReconfigure &&
		    (StringUtils.equals(name, GuiceProperties.LOG4J_PROPERTIES_FILE) ||
		     StringUtils.equals(name, GuiceProperties.LOGBACK_CONFIG_FILE)))
			autoReconfigure(this.guiceConfig);
	}
}
