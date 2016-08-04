package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import com.peterphi.std.io.PropertyFile;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.types.Timebase;
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
import java.util.Collections;
import java.util.HashMap;

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
	protected final GuiceConfig configuration;


	/**
	 * For unit tests only!
	 *
	 * @param properties
	 */
	public ServicePropertiesModule(PropertyFile properties)
	{
		this(new GuiceConfig(Collections.singletonList(properties.toMap()), new HashMap<String, String>(0)));
	}


	public ServicePropertiesModule(GuiceConfig configuration)
	{
		this.configuration = configuration;
	}


	private boolean isList(final ConfigRef ref)
	{
		return ref.getName().endsWith("[]");
	}


	@Override
	@SuppressWarnings("unchecked")
	protected void configure()
	{
		for (String key : configuration.names())
		{
			final ConfigRef ref = new ConfigRef(configuration, key);

			final Named name = Names.named(key);

			bind(ConfigRef.class).annotatedWith(name).toInstance(ref);

			// If the config value is a String then bind it to a variety of different conversion providers
			if (!isList(ref))
			{
				bind(String.class).annotatedWith(name).toProvider(ref);

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
					bind(clazz).annotatedWith(name).toProvider(ref.as(clazz));
				}
			}
		}
	}


	@Provides
	@Singleton
	public GuiceConfig getConfiguration()
	{
		return this.configuration;
	}
}
