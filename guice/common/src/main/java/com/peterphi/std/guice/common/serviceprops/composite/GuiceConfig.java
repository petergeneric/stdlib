package com.peterphi.std.guice.common.serviceprops.composite;

import com.peterphi.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

public class GuiceConfig
{
	private final Map<String, String> properties = new HashMap<>();

	private final Map<String, String> overrides = new HashMap<>(0);

	private final StrSubstitutor substitutor = new StrSubstitutor(new GuiceConfigVariableResolver(this));


	public GuiceConfig()
	{
	}


	public GuiceConfig(final List<Map<String, String>> properties, final Map<String, String> overrides)
	{
		for (Map<String, String> map : properties)
			setAll(map);

		// Set the overrides
		setOverrides(overrides);
	}


	public Set<String> names()
	{
		Set<String> keys = new HashSet<>(properties.keySet());

		keys.addAll(overrides.keySet());

		return keys;
	}


	public void setAll(Properties properties)
	{
		setAll(new PropertyFile(properties));
	}


	public void setAll(PropertyFile properties)
	{
		setAll(properties.toMap());
	}


	public void setAll(Map<String, String> properties)
	{
		if (properties != null)
			this.properties.putAll(properties);
	}


	public void setAll(GuiceConfig other)
	{
		for (String name : other.names())
		{
			String value = other.getRaw(name, null);

			if (value != null)
				set(name, value);
		}
	}


	public void set(String name, final String value)
	{
		properties.put(name, value);
	}


	public Map<String, String> getOverrides()
	{
		return Collections.unmodifiableMap(new HashMap<>(overrides));
	}


	public void setOverride(final String name, final String value)
	{
		overrides.put(name, value);
	}


	public void setOverrides(final Map<String, String> properties)
	{
		if (properties != null)
			overrides.putAll(properties);
	}


	/**
	 * Get a raw value without evaluating any expressions
	 *
	 * @param name
	 * @param defaultValue
	 *
	 * @return
	 */
	public String getRaw(final String name, String defaultValue)
	{
		return overrides.getOrDefault(name, properties.getOrDefault(name, defaultValue));
	}


	public String get(final String name)
	{
		return get(name, null);
	}


	public String get(final String name, final String defaultValue)
	{
		final String raw = getRaw(name, null);

		if (raw == null)
			return defaultValue;
		else
			return substitutor.replace(raw);
	}


	public List<String> getList(final String name, final List<String> defaultValue)
	{
		final String value = get(name, null);

		if (value == null)
			return defaultValue;
		else
			return Arrays.asList(StringUtils.split(value, ','));
	}


	public boolean containsKey(final String name)
	{
		return getRaw(name, null) != null;
	}


	public Boolean getBoolean(final String name, final Boolean defaultValue)
	{
		final String value = get(name, null);

		if (value == null)
			return defaultValue;
		else if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "yes"))
			return true;
		else if (StringUtils.equalsIgnoreCase(value, "false") || StringUtils.equalsIgnoreCase(value, "no"))
			return false;
		else
			throw new IllegalArgumentException("Error parsing property " +
			                                   name +
			                                   "=" +
			                                   value +
			                                   " as boolean (expected true|yes or false|no)!");
	}


	public Map<String, String> toMap(Predicate<String> keySelector)
	{
		final Map<String, String> map = new HashMap<>();

		for (String key : names())
		{
			if (keySelector == null || keySelector.test(key))
				map.put(key, get(key));
		}

		return map;
	}


	public Properties toProperties(Predicate<String> keySelector)
	{
		Properties properties = new Properties();

		properties.putAll(toMap(keySelector));

		return properties;
	}


	public PropertyFile toPropertyFile(Predicate<String> keySelector)
	{
		final Map<String, String> map = toMap(keySelector);

		return new PropertyFile(map);
	}


	public Map<String, String> toMap()
	{
		return toMap(null);
	}


	public Properties toProperties()
	{
		return toProperties(null);
	}


	public PropertyFile toPropertyFile()
	{
		return toPropertyFile(null);
	}
}
