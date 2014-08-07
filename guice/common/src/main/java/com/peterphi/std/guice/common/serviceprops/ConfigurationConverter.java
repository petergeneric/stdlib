package com.peterphi.std.guice.common.serviceprops;

import com.peterphi.std.io.PropertyFile;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

import java.util.Iterator;
import java.util.Properties;

public class ConfigurationConverter
{
	public static Properties toProperties(Configuration configuration)
	{
		Properties properties = new Properties();

		Iterator<String> it = configuration.getKeys();
		while (it.hasNext())
		{
			final String key = it.next();

			properties.put(key, configuration.getString(key));
		}

		return properties;
	}


	/**
	 * Combine a number of PropertyFile instances together to form a single Configuration; the first PropertyFile will be
	 * overridden by configuration in each subsequent PropertyFile
	 *
	 * @param propertyFiles
	 *
	 * @return
	 */
	public static Configuration union(PropertyFile... propertyFiles)
	{
		CompositeConfiguration configuration = new CompositeConfiguration();

		for (int i = propertyFiles.length - 1; i >= 0; i--)
		{
			final PropertyFile file = propertyFiles[i];

			configuration.addConfiguration(new MapConfiguration(file.toProperties()));
		}

		return configuration;
	}
}
