package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQConstraints;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQGroupType;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQOrder;
import com.peterphi.std.guice.restclient.jaxb.webquery.WQUriControlField;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import org.apache.commons.lang.StringUtils;

/**
 * Converts a WebQuery to a ResultSetConstraint (ResultSetConstraint is a legacy type and should not be used in new code)
 */
class WebQueryToResultSetConstraintConverter
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
	public static ResultSetConstraint convert(WebQuery query)
	{
		final ResultSetConstraintBuilder builder = new ResultSetConstraintBuilderFactory().builder();

		builder.setFetch(query.fetch)
		       .setExpand(list(query.expand))
		       .setOrder(query.orderings.stream()
		                                .map(WQOrder:: toLegacyForm)
		                                .toArray(String[] ::new))
		       .limit(query.constraints.limit)
		       .offset(query.constraints.offset);

		if (query.constraints.computeSize)
			builder.replace(WQUriControlField.COMPUTE_SIZE, String.valueOf(query.constraints.computeSize));
		if (query.constraints.subclass != null)
			builder.replace(WQUriControlField.CLASS, list(query.constraints.subclass));

		addConstraints(builder, query.constraints);

		return builder.build();
	}


	private static void addConstraints(final ResultSetConstraintBuilder builder, final WQConstraints constraints)
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
				else if (g.constraints.stream().map(l -> ((WQConstraint) l).field).distinct().count() == 1)
					throw new IllegalArgumentException("Can only convert OR groups containing same field name to legacy ResultSetConstraint type!");

				// Add all the constraints
				g.constraints.stream().map(l -> (WQConstraint) l).forEach(c -> builder.add(c.field, c.encodeValue()));
			}
		}
	}


	private static String[] list(String s)
	{
		if (StringUtils.isBlank(s))
			return new String[]{};
		else
			return s.split(",");
	}
}
