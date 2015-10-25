package com.peterphi.std.guice.hibernate.webquery;

import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryCombiningOperator;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryConstraint;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryConstraintGroup;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryConstraintLine;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryConstraints;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryDefinition;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQueryOrder;
import org.apache.commons.lang.StringUtils;

class ResultSetConstraintToWebQueryDefinitionConverter
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
	public static ResultSetConstraint convert(WebQueryDefinition query)
	{
		final ResultSetConstraintBuilder builder = new ResultSetConstraintBuilderFactory().builder();

		builder.setFetch(query.fetch)
		       .setExpand(list(query.expand))
		       .setOrder(query.orderings.stream()
		                                .map(WebQueryOrder:: toLegacyForm)
		                                .toArray(String[] ::new))
		       .limit(query.constraints.limit)
		       .offset(query.constraints.offset);

		if (query.constraints.computeSize)
			builder.replace(WebQuerySpecialField.COMPUTE_SIZE, String.valueOf(query.constraints.computeSize));
		if (query.constraints.subclass != null)
			builder.replace(WebQuerySpecialField.CLASS, list(query.constraints.subclass));

		addConstraints(builder, query.constraints);

		return builder.build();
	}


	private static void addConstraints(final ResultSetConstraintBuilder builder, final WebQueryConstraints constraints)
	{
		for (WebQueryConstraintLine line : constraints.constraints)
		{
			if (line instanceof WebQueryConstraint)
			{
				WebQueryConstraint c = (WebQueryConstraint) line;

				builder.add(c.field, c.encodeValue());
			}
			else if (line instanceof WebQueryConstraintGroup)
			{
				WebQueryConstraintGroup g = (WebQueryConstraintGroup) line;

				if (g.operator != WebQueryCombiningOperator.OR)
					throw new IllegalArgumentException("Can only convert OR groups to legacy ResultSetConstraint type!");
				else if (!g.constraints.stream().allMatch(l -> l instanceof WebQueryConstraint))
					throw new IllegalArgumentException("Can only convert un-nested groups to legacy ResultSetConstraint type!");
				else if (g.constraints.stream().map(l -> ((WebQueryConstraint) l).field).distinct().count() == 1)
					throw new IllegalArgumentException("Can only convert OR groups containing same field name to legacy ResultSetConstraint type!");

				// Add all the constraints
				g.constraints.stream().map(l -> (WebQueryConstraint) l).forEach(c -> builder.add(c.field, c.encodeValue()));
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
