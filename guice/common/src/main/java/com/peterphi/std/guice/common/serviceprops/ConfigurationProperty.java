package com.peterphi.std.guice.common.serviceprops;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigurationProperty
{
	private final ConfigurationPropertyRegistry registry;
	private final Configuration configuration;
	private final CopyOnWriteArrayList<ConfigurationPropertyBindingSite> bindings = new CopyOnWriteArrayList<>();

	private final String name;


	public ConfigurationProperty(final ConfigurationPropertyRegistry registry,
	                             final Configuration configuration,
	                             final String name)
	{
		this.registry = registry;
		this.configuration = configuration;
		this.name = name;
	}


	public void add(ConfigurationPropertyBindingSite site)
	{
		bindings.add(site);
	}


	public String getName()
	{
		return name;
	}


	public Class<?> getType()
	{
		for (ConfigurationPropertyBindingSite binding : bindings)
			return binding.getType();

		return null; // will never hit this, must always have one binding
	}


	public List<ConfigurationPropertyBindingSite> getBindings()
	{
		return bindings;
	}


	public boolean isDeprecated()
	{
		for (ConfigurationPropertyBindingSite binding : bindings)
			if (binding.isDeprecated())
				return true;

		// None are deprecated
		return false;
	}


	public String getDocumentation()
	{
		for (ConfigurationPropertyBindingSite binding : bindings)
		{
			final String description = binding.getDescription();

			if (!StringUtils.isEmpty(description))
				return description;
		}

		// No description
		return null;
	}


	public Collection<String> getHrefs()
	{
		TreeSet<String> allHrefs = new TreeSet<String>();

		for (ConfigurationPropertyBindingSite binding : bindings)
		{
			String[] hrefs = binding.getHrefs();

			if (hrefs != null)
				allHrefs.addAll(Arrays.asList(hrefs));
		}

		return allHrefs;
	}


	public void set(final String value)
	{
		// Validate the new value passes all the binding constraints
		validate(value);

		// Add a property override to the configuration
		configuration.setProperty(name, value);

		// Reinject all the members
		for (ConfigurationPropertyBindingSite binding : bindings)
			binding.reinject(registry.getInstances(binding.getOwner()));
	}


	public void validate(final String value)
	{
		// Validate the new value passes all the binding constraints
		for (ConfigurationPropertyBindingSite binding : bindings)
			binding.validate(value);
	}
}
