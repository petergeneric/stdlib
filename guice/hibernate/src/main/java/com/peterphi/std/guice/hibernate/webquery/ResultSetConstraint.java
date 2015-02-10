package com.peterphi.std.guice.hibernate.webquery;

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


	public Map<String, List<String>> getParameters()
	{
		return parameters;
	}


	/**
	 * Get all parameters except {@link com.peterphi.std.guice.hibernate.webquery.WebQuerySpecialField#OFFSET} and {@link
	 * com.peterphi.std.guice.hibernate.webquery.WebQuerySpecialField#LIMIT}<br />
	 * The returned map should not be modified.
	 *
	 * @return
	 */
	public Map<String, List<String>> getOtherParameters()
	{
		Map<String, List<String>> map = new HashMap<>(getParameters());

		map.remove(WebQuerySpecialField.OFFSET.getName());
		map.remove(WebQuerySpecialField.LIMIT.getName());

		return map;
	}
}
