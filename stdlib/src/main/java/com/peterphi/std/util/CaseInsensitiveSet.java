package com.peterphi.std.util;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A case-insensitive case-preserving String Set
 */
public class CaseInsensitiveSet extends TreeSet<String>
{
	public CaseInsensitiveSet()
	{
		super(String.CASE_INSENSITIVE_ORDER);
	}


	public CaseInsensitiveSet(Collection<String> existing)
	{
		this();

		if (existing != null)
			this.addAll(existing);
	}


	public boolean notContains(final String val)
	{
		return !contains(val);
	}


	public static CaseInsensitiveSet of(String... values)
	{
		return new CaseInsensitiveSet(Set.of(values));
	}


	/**
	 * Parse a simple comma-separated representation; leading and trailing whitespace will be stripped from elements; empty items will be ignored
	 *
	 * @param encoded
	 * @return
	 */
	public static CaseInsensitiveSet valueOf(final String encoded)
	{
		if (StringUtils.isBlank(encoded))
			return new CaseInsensitiveSet();
		else
			return Arrays
					       .stream(StringUtils.split(encoded, ','))
					       .map(StringUtils :: trimToNull)
					       .filter(Objects :: nonNull)
					       .collect(Collectors.toCollection(CaseInsensitiveSet :: new));
	}
}
