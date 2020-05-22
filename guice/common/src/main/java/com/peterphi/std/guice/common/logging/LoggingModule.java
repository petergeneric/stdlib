package com.peterphi.std.guice.common.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.AbstractModule;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfigChangeObserver;
import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

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


	private interface JoranTask
	{
		void apply(JoranConfigurator joran) throws JoranException, IOException;
	}


	public LoggingModule(GuiceConfig configuration)
	{
		this.guiceConfig = configuration;

		configuration.registerChangeObserver(this);
	}


	@Override
	protected void configure()
	{
		reconfigure(guiceConfig);
	}


	public static void manualReconfigure(final GuiceConfig guice)
	{
		log.info("Manual reconfiguration of log4j.properties - disabling automatic reload!");
		allowAutoReconfigure = false;
		reconfigure(guice);
	}


	public static void autoReconfigure(final GuiceConfig guice)
	{
		if (allowAutoReconfigure)
			reconfigure(guice);
	}


	public static void setAllowAutoReconfigure(final boolean value)
	{
		allowAutoReconfigure = value;
	}


	public static boolean getAllowAutoReconfigure()
	{
		return allowAutoReconfigure;
	}


	private static void reconfigure(final GuiceConfig config)
	{
		final String logbackConfig = config.get(GuiceProperties.LOGBACK_CONFIG_FILE, null);

		// No logback config - fall back on translating log4j
		if (logbackConfig == null)
		{
			boolean success;
			try
			{
				success = tryLog4jPropertiesFallback(config);
			}
			catch (Throwable t)
			{
				log.warn("Exception occurred trying to translate log4j.properties!", t);
				success = false;
			}

			if (!success)
				log.warn("Leaving logback to configure itself");

			return;
		}

		try
		{
			// Inline XML
			if (StringUtils.startsWith(logbackConfig, "<"))
			{
				log.debug("Assuming logback.xml contains literal logback XML file, not a resource/file reference");

				doLogbackConfig(joran -> joran.doConfigure(new InputSource(new StringReader(logbackConfig))));
			}
			else if (isLocalFile(logbackConfig))
			{
				doLogbackConfig(joran -> joran.doConfigure(new File(logbackConfig)));
			}
			else
			{
				final URL resource = LoggingModule.class.getResource(logbackConfig);

				if (resource != null)
				{
					if (resource.getProtocol().equalsIgnoreCase("file"))
					{
						final File file = new File(resource.toURI());

						doLogbackConfig(joran -> joran.doConfigure(file));
					}
					else
					{
						doLogbackConfig(joran -> joran.doConfigure(resource));
					}
				}
				else
				{
					log.warn("Invalid logback configuration file provided, using defaults. Requested config was: " +
					         logbackConfig);
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Error initiaising logging configuration: " + e.getMessage(), e);
		}
	}


	private static boolean isLocalFile(final String value)
	{
		final File f = new File(value);

		return (f.isAbsolute() && f.exists() && f.isFile());
	}


	private static void doLogbackConfig(JoranTask loader) throws IOException
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

			loader.apply(configurator);
		}
		catch (JoranException je)
		{
			throw new RuntimeException(je);
			// StatusPrinter will handle this
		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}


	private static boolean tryLog4jPropertiesFallback(final GuiceConfig config) throws IOException
	{
		PropertyFile log4jConfig = getProperties(config);

		if (log4jConfig != null)
		{
			final String logbackConfig = Log4jToLogbackConverter.convert(log4jConfig);

			if (logbackConfig != null)
			{
				doLogbackConfig(joran -> joran.doConfigure(new InputSource(new StringReader(logbackConfig))));
				return true;
			}
		}

		return false; // No log4j configuration to convert
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
