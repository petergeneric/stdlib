package com.peterphi.std.guice.hibernate.webquery;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a web query constraint set independent of wire representation
 */
public class ResultSetConstraint
{
	private Map<String, List<String>> parameters = new HashMap<>();


	public ResultSetConstraint(Map<String, List<String>> queryString, int defaultLimit)
	{
		parameters.put(WebQuerySpecialField.OFFSET.getName(), Arrays.asList("0"));
		parameters.put(WebQuerySpecialField.LIMIT.getName(), Arrays.asList(Integer.toString(defaultLimit)));

		parameters.putAll(queryString);
	}


	public int getOffset()
	{
		return Integer.parseInt(parameters.get(WebQuerySpecialField.OFFSET.getName()).get(0));
	}


	public int getLimit()
	{
		return Integer.parseInt(parameters.get(WebQuerySpecialField.LIMIT.getName()).get(0));
	}


	public boolean isComputeSize()
	{
		List<String> values = parameters.get(WebQuerySpecialField.COMPUTE_SIZE.getName());

		if (values == null || values.isEmpty())
			return false;
		else if (values.size() == 1)
			return parseBoolean(values.get(0));
		else
			throw new IllegalArgumentException("Expected exactly 1 value for " +
			                                   WebQuerySpecialField.COMPUTE_SIZE.getName() +
			                                   ", got: " +
			                                   values);
	}


	public Map<String, List<String>> getParameters()
	{
		return parameters;
	}


	/**
	 * Get all parameters except {@link com.peterphi.std.guice.hibernate.webquery.WebQuerySpecialField#OFFSET}, {@link
	 * com.peterphi.std.guice.hibernate.webquery.WebQuerySpecialField#LIMIT} and {@link WebQuerySpecialField#COMPUTE_SIZE}<br />
	 * The returned map should not be modified.
	 *
	 * @return
	 */
	public Map<String, List<String>> getOtherParameters()
	{
		Map<String, List<String>> map = new HashMap<>(getParameters());

		map.remove(WebQuerySpecialField.OFFSET.getName());
		map.remove(WebQuerySpecialField.LIMIT.getName());
		map.remove(WebQuerySpecialField.COMPUTE_SIZE.getName());

		return map;
	}


	private static boolean parseBoolean(String value)
	{
		if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "yes") ||
		    StringUtils.equalsIgnoreCase(value, "on"))
			return true;
		else if (StringUtils.equalsIgnoreCase(value, "false") || StringUtils.equalsIgnoreCase(value, "no") ||
		         StringUtils.equalsIgnoreCase(value, "off"))
			return false;
		else
			throw new IllegalArgumentException("Cannot parse boolean: " + value);
	}
}
