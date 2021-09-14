package com.peterphi.std.guice.restclient.jaxb.webquery.plugin;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class DenyKeyPlugin implements WebQueryDecodePlugin
{
	private final Set<String> keys;
	private final boolean isBanList;
	private final boolean ignoreSpecial;


	public DenyKeyPlugin(Collection<String> keys, final boolean isBanList, final boolean ignoreSpecial)
	{
		this.keys = keys.stream().filter(Objects :: nonNull).map(k -> k.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
		this.isBanList = isBanList;
		this.ignoreSpecial = ignoreSpecial;
	}


	@Override
	public boolean handles(final String key)
	{
		if (ignoreSpecial && (key.charAt(0) == '_' || key.equalsIgnoreCase("q")))
			return false; // asked to ignore special

		final boolean onList = keys.contains(key.toLowerCase(Locale.ROOT));

		if (isBanList && onList) // ban list and present
			throw new IllegalArgumentException("Prohibited field: " + key);
		else if (!isBanList && !onList) // allow list but not present
			throw new IllegalArgumentException("Field not permitted: " + key);

		return false;
	}


	@Override
	public void process(final WebQuery query, final String key, final List<String> values)
	{
		handles(key);
	}
}
