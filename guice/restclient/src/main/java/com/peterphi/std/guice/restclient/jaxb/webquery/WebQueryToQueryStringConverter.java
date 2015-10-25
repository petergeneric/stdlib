package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a WebQuery to the parameters for a query string query
 */
class WebQueryToQueryStringConverter
{
	/**
	 * Convert a WebQueryDefinition to the equivalent legacy ResultSetConstraint (if possible)
	 *
	 * @param query
	 *
	 * @return
	 *
	 * @throws IllegalArgumentException
	 * 		if the provided query definition cannot be represented using legacy semantics
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, List<String>> convert(WebQuery query)
	{
		MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();

		map.putSingle(WQUriControlField.FETCH.getName(), query.fetch);
		map.put(WQUriControlField.EXPAND.getName(), list(query.expand));
		map.put(WQUriControlField.ORDER.getName(),
		        query.orderings.stream().map(WQOrder:: toLegacyForm).collect(Collectors.toList()));
		map.putSingle(WQUriControlField.OFFSET.getName(), String.valueOf(query.getOffset()));
		map.putSingle(WQUriControlField.LIMIT.getName(), String.valueOf(query.getLimit()));

		if (query.constraints.computeSize)
			map.putSingle(WQUriControlField.COMPUTE_SIZE.getName(), String.valueOf(query.constraints.computeSize));
		if (query.constraints.subclass != null)
			map.put(WQUriControlField.CLASS.getName(), list(query.constraints.subclass));

		addConstraints(map, query.constraints);

		return map;
	}


	private static void addConstraints(final MultivaluedHashMap<String, String> builder, final WQConstraints constraints)
	{
		for (WQConstraintLine line : constraints.constraints)
		{
			if (line instanceof WQConstraint)
			{
				WQConstraint c = (WQConstraint) line;

				builder.add(c.field, c.encodeValue());
			}
			else if (line instanceof WQGroup)
			{
				WQGroup g = (WQGroup) line;

				if (g.operator != WQGroupType.OR)
					throw new IllegalArgumentException("Can only convert OR groups to legacy ResultSetConstraint type!");
				else if (!g.constraints.stream().allMatch(l -> l instanceof WQConstraint))
					throw new IllegalArgumentException("Can only convert un-nested groups to legacy ResultSetConstraint type!");
				else if (g.constraints.stream()
				                      .map(l -> ((WQConstraint) l).field)
				                      .distinct()
				                      .collect(Collectors.toList())
				                      .size() > 1)
					throw new IllegalArgumentException("Can only convert OR groups containing same field name to legacy ResultSetConstraint type! Fields: " +
					                                   g.constraints.stream()
					                                                .map(l -> ((WQConstraint) l).field)
					                                                .distinct()
					                                                .collect(Collectors.toList()));

				// Add all the constraints
				g.constraints.stream().map(l -> (WQConstraint) l).forEach(c -> builder.add(c.field, c.encodeValue()));
			}
		}
	}


	private static List<String> list(String s)
	{
		if (StringUtils.isBlank(s))
			return Collections.emptyList();
		else
			return new ArrayList<>(Arrays.asList(s.split(",")));
	}
}
