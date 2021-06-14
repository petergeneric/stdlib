package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.List;

class UnionPlugin implements WebQueryDecodePlugin
{
	private final WebQueryDecodePlugin[] plugins;

	public UnionPlugin(final WebQueryDecodePlugin... plugins)
	{
		this.plugins = plugins;
	}


	@Override
	public boolean handles(final String key)
	{
		for (WebQueryDecodePlugin plugin : plugins)
		{
			if (plugin != null && plugin.handles(key))
				return true;
		}

		return false;
	}


	@Override
	public void process(final WebQuery query, final String key, final List<String> values)
	{
		for (WebQueryDecodePlugin plugin : plugins)
		{
			if (plugin != null && plugin.handles(key))
			{
				plugin.process(query, key, values);
			}
		}
	}
}
