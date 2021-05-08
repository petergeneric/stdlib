package com.peterphi.std.guice.restclient.jaxb.webquery;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Special WebQuery fields and their wire representation in the Query String API
 */
public enum WQUriControlField
{
	/**
	 * Set the index of the first result to return for this query
	 */
	OFFSET("_offset"),
	/**
	 * Set to the maximum results to return for this query; a special limit of -1 (see {@link WebQuery#LIMIT_RETURN_ZERO})
	 * requests no row data (useful when just wanting a count of resultset size.
	 */
	LIMIT("_limit"),
	ORDER("_order"),
	/**
	 * Set to true to request the resultset size be computed
	 */
	COMPUTE_SIZE("_compute_size"),
	/**
	 * Set to true to request that the SQL prepared statements as a result of this query be recorded as part of the resultset
	 */
	LOG_SQL("_log_sql"),
	/**
	 * Set to true to request performance metrics be reported for this query
	 */
	LOG_PERFORMANCE("_log_performance"),
	/**
	 * Set to some class to specify the subclass to return (for entities with type hierarchies)
	 */
	CLASS("_class"),

	/**
	 * Set to some comma-separated list of relationships to expand. This is handled after the query completes and the results are
	 * being serialised
	 */
	EXPAND("_expand"),
	/**
	 * Set to <code>entity</code> (the default) or <code>id</code> to fetch back the entity or just the entity primary key. This
	 * is handled after the query completes and the results are being serialised
	 */
	FETCH("_fetch"),
	/**
	 * Controls which relationships are eagerly fetched during the database query. <strong>N.B. the more relationships that are
	 * eagerly fetched the larger the db result set and therefore the slower the query will perform - strike a balance and take
	 * account of the value of <code>expand</code></strong>
	 */
	DBFETCH("_dbfetch"),
	/**
	 * An optional name for the query, to allow server-side optimisation/hinting
	 */
	NAME("_name"),
	TEXT_QUERY("q");

	private final String name;


	WQUriControlField(final String name)
	{
		this.name = name;
	}


	/**
	 * Return the name of this field when used in maps
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}


	public static WQUriControlField getByName(final String fieldName)
	{
		for (WQUriControlField field : values())
		{
			if (field.name.equals(fieldName))
				return field;
		}

		throw new IllegalArgumentException("No core WebQueryField with name: " + fieldName);
	}


	/**
	 * Return all the permitted names
	 *
	 * @return
	 */
	public static List<String> getAllNames()
	{
		return Arrays.asList(values()).stream().map(o -> o.getName()).collect(Collectors.toList());
	}
}
