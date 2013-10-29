package com.peterphi.std.guice.hibernate.webquery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetConstraint
{
	private Map<String, List<String>> parameters = new HashMap<>();


	public ResultSetConstraint(Map<String, List<String>> queryString, int defaultLimit)
	{
		parameters.put("_offset", Arrays.asList("0"));
		parameters.put("_limit", Arrays.asList(Integer.toString(defaultLimit)));

		parameters.putAll(queryString);
	}


	public int getOffset()
	{
		return Integer.parseInt(parameters.get("_offset").get(0));
	}


	public int getLimit()
	{
		return Integer.parseInt(parameters.get("_limit").get(0));
	}


	public Map<String, List<String>> getParameters()
	{
		return parameters;
	}
}
