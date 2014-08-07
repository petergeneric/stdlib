package com.peterphi.std.guice.common.serviceprops;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationProperty
{
	private final String name;
	private final List<BindingSite> bindings = new ArrayList<>();


	public ConfigurationProperty(final String name)
	{
		this.name = name;
	}


	public void add(BindingSite site)
	{
		bindings.add(site);
	}


	public String getName()
	{
		return name;
	}


	public Class<?> getType()
	{
		for (BindingSite binding : bindings)
			return binding.getType();

		return null; // will never hit this, must always have one binding
	}


	public String getDocumentation()
	{
		for (BindingSite binding : bindings)
		{
			final String description = binding.getDescription();

			if (!StringUtils.isEmpty(description))
				return description;
		}

		// No description
		return null;
	}
}
