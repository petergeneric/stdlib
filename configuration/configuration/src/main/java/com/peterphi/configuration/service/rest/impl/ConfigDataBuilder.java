package com.peterphi.configuration.service.rest.impl;

import com.peterphi.std.guice.config.rest.types.ConfigPropertyValue;

import java.util.HashMap;
import java.util.Map;

public class ConfigDataBuilder
{
	private Map<String, Map<String, ConfigPropertyValue>> values = new HashMap<>();


	public void set(String path, String name, String value)
	{
		Map<String, ConfigPropertyValue> properties = values.get(path);

		if (properties == null)
		{
			properties = new HashMap<>();
			values.put(path, properties);
		}

		properties.put(name, new ConfigPropertyValue(path, name, value));
	}


	public Map<String, Map<String, ConfigPropertyValue>> build()
	{
		return values;
	}
}
