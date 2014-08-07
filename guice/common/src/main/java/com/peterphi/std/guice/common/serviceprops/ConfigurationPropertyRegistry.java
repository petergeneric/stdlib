package com.peterphi.std.guice.common.serviceprops;

import org.apache.log4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationPropertyRegistry
{
	private static final Logger log = Logger.getLogger(ConfigurationPropertyRegistry.class);

	private final Map<String, ConfigurationProperty> properties = new HashMap<>();


	public <T> void register(final Class<?> owner, String name, Class<T> type, AnnotatedElement element)
	{
		register(new BindingSite(owner, name, type, element));
	}


	public <T> void register(BindingSite<T> site)
	{
		if (!properties.containsKey(site.getName()))
		{
			log.debug("Discovered new property: " + site.getName());

			properties.put(site.getName(), new ConfigurationProperty(site.getName()));
		}

		log.trace("Discovered new binding for property " +
		          site.getName() +
		          " of type " +
		          site.getType() +
		          " in " +
		          site.getOwner());

		properties.get(site.getName()).add(site);
	}

	public ConfigurationProperty get(String name) {
		return properties.get(name);
	}

	public Collection<ConfigurationProperty> getAll() {
		return properties.values();
	}
}
