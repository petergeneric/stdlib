package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryDefinition;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a web query constraint set independent of wire representation
 */
public class ResultSetConstraint
{
	private Map<String, List<String>> parameters = new HashMap<>();


	public ResultSetConstraint(Map<String, List<String>> parameters)
	{
		this.parameters = parameters;
	}


	/**
	 * @param queryString
	 * @param defaultLimit
	 *
	 * @deprecated use {@link ResultSetConstraintBuilder} instead
	 */
	@Deprecated
	public ResultSetConstraint(Map<String, List<String>> queryString, int defaultLimit)
	{
		this(queryString, null, defaultLimit);
	}


	/**
	 * Note, regular consumers should use {@link ResultSetConstraintBuilder} instead
	 *
	 * @param queryString
	 * @param defaultOrder
	 * @param defaultLimit
	 *
	 * @deprecated use {@link ResultSetConstraintBuilder} instead
	 */
	@Deprecated
	public ResultSetConstraint(Map<String, List<String>> queryString, List<String> defaultOrder, int defaultLimit)
	{
		if (defaultOrder != null && !defaultOrder.isEmpty())
			parameters.put(WebQuerySpecialField.ORDER.getName(), defaultOrder);

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


	/**
	 * Retrieve the {@link WebQuerySpecialField#EXPAND} parameter as a Set (or "all" if none is defined)
	 *
	 * @return
	 */
	public Set<String> getExpand()
	{
		List<String> values = parameters.get(WebQuerySpecialField.EXPAND.getName());

		if (values == null || values.isEmpty())
			return Collections.singleton("all");
		else
			return new HashSet<>(values);
	}


	/**
	 * Retrieve the {@link WebQuerySpecialField#FETCH} parameter (or "entity" if none is defined)
	 *
	 * @return
	 */
	public String getFetch()
	{
		List<String> values = parameters.get(WebQuerySpecialField.FETCH.getName());

		if (values == null || values.isEmpty())
			return "entity";
		else
			return values.get(0);
	}


	public Map<String, List<String>> getParameters()
	{
		return parameters;
	}


	/**
	 * Get all parameters except those with a name in {@link WebQuerySpecialField}<br />
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
		map.remove(WebQuerySpecialField.FETCH.getName());
		map.remove(WebQuerySpecialField.EXPAND.getName());

		return map;
	}


	/**
	 * Convert this query to a {@link WebQueryDefinition} using the legacy web query semantics
	 *
	 * @return
	 */
	public WebQueryDefinition toQuery()
	{
		return new ResultSetConstraintBuilderFactory().builder(getParameters()).buildQuery();
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
