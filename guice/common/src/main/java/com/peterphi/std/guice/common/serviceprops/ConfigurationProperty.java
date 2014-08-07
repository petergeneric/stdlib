package com.peterphi.std.guice.common.serviceprops;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class ConfigurationProperty
{
	private final String name;
	private final List<ConfigurationPropertyBindingSite> bindings = new ArrayList<>();


	public ConfigurationProperty(final String name)
	{
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
		return false; // TODO implement me
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
}
