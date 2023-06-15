package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.List;
import java.util.function.Predicate;

class ValidateKeyPlugin implements WebQueryDecodePlugin
{
	private final String key;
	private final Predicate<String> predicate;


	public ValidateKeyPlugin(final String key, final Predicate<String> predicate)
	{
		this.key = key;
		this.predicate = predicate;
	}


	@Override
	public boolean handles(final String key, final List<String> values)
	{
		if (key.equalsIgnoreCase(key))
			for (String value : values)
				if (!predicate.test(value))
					throw new IllegalArgumentException("Prohibited value for '" + key + "': " + value);

		return false;
	}


	@Override
	public boolean handles(final String key)
	{
		throw new RuntimeException("Should never be called!");
	}


	@Override
	public void process(final WebQuery query, final String key, final List<String> values)
	{
		handles(key, values);
	}
}
