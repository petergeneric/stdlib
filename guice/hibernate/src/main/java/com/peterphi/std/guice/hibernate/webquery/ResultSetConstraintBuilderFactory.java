package com.peterphi.std.guice.hibernate.webquery;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.peterphi.std.annotation.Doc;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * @deprecated use {@link WebQuery} and the associated builder methods, particularly {@link WebQuery#decode(UriInfo)}
 */
@Singleton
@Deprecated
public class ResultSetConstraintBuilderFactory
{
	@Inject(optional = true)
	@Named("resultset.default-limit")
	@Doc("The default limit to use for web queries (default 200)")
	int defaultLimit = 200;


	/**
	 * Convenience method to build based on a Map of constraints quickly
	 *
	 * @param constraints
	 *
	 * @return
	 *
	 * @deprecated use {@link #builder(Map)} and then call {@link ResultSetConstraintBuilder#buildQuery()}
	 */
	@Deprecated
	public ResultSetConstraint build(Map<String, List<String>> constraints)
	{
		return builder(constraints).build();
	}


	public ResultSetConstraintBuilder builder(Map<String, List<String>> constraints)
	{
		return builder().add(constraints);
	}


	/**
	 * Construct a new builder with no pre-defined constraints
	 *
	 * @return
	 */
	public ResultSetConstraintBuilder builder()
	{
		return new ResultSetConstraintBuilder(defaultLimit);
	}
}
