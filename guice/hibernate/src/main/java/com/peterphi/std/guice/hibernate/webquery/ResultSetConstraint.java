package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WQUriControlField;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a web query constraint that ANDs together constraints, but ORs together constraints for the same field<br />
 * This is a direct representation of the limited Query String Query API format
 *
 * @deprecated use {@link WebQuery} instead
 */
@Deprecated
public class ResultSetConstraint
{
	private Map<String, List<String>> parameters = new HashMap<>();


	@Deprecated
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
			parameters.put(WQUriControlField.ORDER.getName(), defaultOrder);

		parameters.put(WQUriControlField.OFFSET.getName(), Arrays.asList("0"));
		parameters.put(WQUriControlField.LIMIT.getName(), Arrays.asList(Integer.toString(defaultLimit)));

		parameters.putAll(queryString);
	}


	public int getOffset()
	{
		return Integer.parseInt(parameters.get(WQUriControlField.OFFSET.getName()).get(0));
	}


	public int getLimit()
	{
		return Integer.parseInt(parameters.get(WQUriControlField.LIMIT.getName()).get(0));
	}


	public boolean isComputeSize()
	{
		List<String> values = parameters.get(WQUriControlField.COMPUTE_SIZE.getName());

		if (values == null || values.isEmpty())
			return false;
		else if (values.size() == 1)
			return parseBoolean(values.get(0));
		else
			throw new IllegalArgumentException("Expected exactly 1 value for " +
			                                   WQUriControlField.COMPUTE_SIZE.getName() +
			                                   ", got: " +
			                                   values);
	}


	/**
	 * Retrieve the {@link WQUriControlField#EXPAND} parameter as a Set (or "all" if none is defined)
	 *
	 * @return
	 */
	public Set<String> getExpand()
	{
		List<String> values = parameters.get(WQUriControlField.EXPAND.getName());

		if (values == null || values.isEmpty())
			return Collections.singleton("all");
		else
			return new HashSet<>(values);
	}


	/**
	 * Retrieve the {@link WQUriControlField#FETCH} parameter (or "entity" if none is defined)
	 *
	 * @return
	 */
	public String getFetch()
	{
		List<String> values = parameters.get(WQUriControlField.FETCH.getName());

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
	 * Get all parameters except those with a name in {@link WQUriControlField}<br />
	 * The returned map should not be modified.
	 *
	 * @return
	 */
	public Map<String, List<String>> getOtherParameters()
	{
		Map<String, List<String>> map = new HashMap<>(getParameters());

		map.remove(WQUriControlField.OFFSET.getName());
		map.remove(WQUriControlField.LIMIT.getName());
		map.remove(WQUriControlField.COMPUTE_SIZE.getName());
		map.remove(WQUriControlField.FETCH.getName());
		map.remove(WQUriControlField.EXPAND.getName());

		return map;
	}


	/**
	 * Convert this query to a {@link WebQuery} using the legacy web query semantics
	 *
	 * @return
	 */
	public WebQuery toQuery()
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
