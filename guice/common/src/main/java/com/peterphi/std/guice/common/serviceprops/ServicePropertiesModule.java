package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.peterphi.std.guice.apploader.GuiceProperties;
import com.peterphi.std.io.PropertyFile;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.types.Timebase;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Module that binds properties from {@link PropertyFile}s in the classpath (called <code>service.properties</code> by
 * default)<br
 * />
 * The properties found in the PropertyFile object(s) are all exposed a String Named properties. A read-only composite
 * PropertyFile is bound as the {@link PropertyFile} Named property
 * "service.properties"
 */
public class ServicePropertiesModule extends AbstractModule
{
	private static final Logger log = Logger.getLogger(ServicePropertiesModule.class);

	/**
	 * A set of property names that are permitted to have a non-string value without a warning being triggered
	 */
	public static final Set<String> NO_WARN_NON_STRING_PROPERTIES = new HashSet<>(Arrays.asList(GuiceProperties.SCAN_PACKAGES));

	protected final CompositeConfiguration configuration;
	protected final PropertiesConfiguration overrides;


	public ServicePropertiesModule(CompositeConfiguration configuration, PropertiesConfiguration overrides)
	{
		this.configuration = configuration;
		this.overrides = overrides;
	}


	@Override
	@SuppressWarnings("unchecked")
	protected void configure()
	{
		Iterator<String> it = configuration.getKeys();

		while (it.hasNext())
		{
			final String key = it.next();
			final Object currentValue = configuration.getProperty(key);

			ConfigRef prop = new ConfigRef(configuration, key);

			final Named name = Names.named(key);

			bind(ConfigRef.class).annotatedWith(name).toInstance(prop);

			// If the config value is a String then bind it to a variety of different conversion providers
			if (currentValue instanceof String)
			{
				bind(String.class).annotatedWith(name).toProvider(prop);

				// Yuck, there has to be a better way...
				for (Class clazz : new Class[]{Boolean.class,
				                               Byte.class,
				                               Short.class,
				                               Integer.class,
				                               Long.class,

				                               Float.class,
				                               Double.class,

				                               Timeout.class,
				                               Timebase.class,

				                               File.class,
				                               InetAddress.class,
				                               URI.class,
				                               URL.class,

				                               DateTime.class,
				                               DateTimeZone.class,
				                               LocalDate.class,
				                               LocalTime.class,
				                               Period.class,
				                               Duration.class,
				                               Interval.class})
				{
					bind(clazz).annotatedWith(name).toProvider(prop.as(clazz));
				}
			}
			else
			{
				// Log about non-string properties (unless they're no warn properties)
				if (!NO_WARN_NON_STRING_PROPERTIES.contains(key))
					log.warn("Non-string property value for " +
					         key +
					         " will only be bound as named ConfigRef type with value: " +
					         currentValue);
			}
		}
	}


	@Provides
	@Singleton
	public Configuration getConfiguration()
	{
		return this.configuration;
	}


	@Provides
	@Singleton
	@Named("overrides")
	public PropertiesConfiguration getOverrideConfiguration()
	{
		return overrides;
	}
}
