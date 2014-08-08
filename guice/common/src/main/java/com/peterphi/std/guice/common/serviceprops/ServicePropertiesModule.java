package com.peterphi.std.guice.common.serviceprops;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.peterphi.std.io.PropertyFile;
import com.peterphi.std.threading.Timeout;
import com.peterphi.std.types.Timebase;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
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
import java.util.HashMap;
import java.util.Iterator;

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
	protected final CompositeConfiguration configuration;
	protected final MapConfiguration overrides;


	public ServicePropertiesModule(Configuration configuration)
	{
		this(configuration, new MapConfiguration(new HashMap<String, Object>()));
	}


	public ServicePropertiesModule(Configuration configuration, MapConfiguration overrides)
	{
		this.configuration = new CompositeConfiguration();
		this.overrides = overrides;

		// Any changes made while running (e.g. through a reconfig service / UI)
		this.configuration.addConfiguration(overrides, true);

		this.configuration.addConfiguration(configuration);
	}


	@Override
	@SuppressWarnings("unchecked")
	protected void configure()
	{
		Iterator<String> it = configuration.getKeys();

		while (it.hasNext())
		{
			final String key = it.next();

			ConfigRef prop = new ConfigRef(configuration, key);

			final Named name = Names.named(key);

			bind(ConfigRef.class).annotatedWith(name).toInstance(prop);
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
	public MapConfiguration getOverrideConfiguration()
	{
		return overrides;
	}
}
