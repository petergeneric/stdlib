package com.peterphi.std.guice.common.serviceprops;

import com.peterphi.std.guice.common.serviceprops.composite.GuiceConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigurationProperty
{
	private static final Logger log = Logger.getLogger(ConfigurationProperty.class);

	private final ConfigurationPropertyRegistry registry;
	private final GuiceConfig configuration;

	private final CopyOnWriteArrayList<ConfigurationPropertyBindingSite> bindings = new CopyOnWriteArrayList<>();

	private final String name;


	public ConfigurationProperty(final ConfigurationPropertyRegistry registry, final GuiceConfig configuration, final String name)
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


	public boolean isFrameworkProperty()
	{
		for (ConfigurationPropertyBindingSite site : bindings)
		{
			if (StringUtils.startsWith(site.getOwner().getPackage().getName(), "com.peterphi.std"))
			{
				return true;
			}
		}

		return false;
	}


	public boolean isReconfigurable()
	{
		for (ConfigurationPropertyBindingSite binding : bindings)
			if (!binding.isReconfigurable())
				return false;

		return true; // all reconfigurable
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
		return "";
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
		log.info("Attempting to change config property " +
		         name +
		         " from current \"" +
		         configuration.get(name) +
		         "\" to \"" +
		         value +
		         "\".");
		// Validate the new value passes all the binding constraints
		validate(value);

		// Add a property override to the configuration
		configuration.setOverride(name, value);

		if (isReconfigurable())
		{
			log.info("All binding sites for property " + name + " are reconfigurable; reinjecting...");

			// Re-inject all the members
			for (ConfigurationPropertyBindingSite binding : bindings)
				binding.reinject(registry.getInstances(binding.getOwner()));
		}
		else
		{
			log.info("Not all binding sites for property " +
			         name +
			         " are reconfigurable. Restart will be required to apply this change.");
		}
	}


	public Set<Object> getBoundValues()
	{
		Set<Object> value = new HashSet<>();

		for (ConfigurationPropertyBindingSite binding : bindings)
			value.addAll(binding.get(registry.getInstances(binding.getOwner())));

		return value;
	}


	public void validate(final String value)
	{
		// Validate the new value passes all the binding constraints
		for (ConfigurationPropertyBindingSite binding : bindings)
			binding.validate(value);
	}


	public String getValue()
	{
		return configuration.get(name);
	}
}
