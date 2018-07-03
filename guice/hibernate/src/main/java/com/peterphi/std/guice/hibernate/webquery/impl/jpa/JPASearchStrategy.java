package com.peterphi.std.guice.hibernate.webquery.impl.jpa;

public enum JPASearchStrategy
{
	/**
	 * Run the query, returning a sorted list of entities that match. The actual underlying strategy may change between {@link
	 * #ENTITY} and {@link #ID_THEN_QUERY_ENTITY} depending on the query and entity configuration
	 */
	AUTO,
	/**
	 * Run the query, returning a sorted list of IDs that match
	 */
	ID,
	/**
	 * Run the query, returning a sorted list of entities that match
	 */
	ENTITY,
	/**
	 * Run the query, returning a sorted list of entities having only the ID field(s) populated
	 */
	ENTITY_WRAPPED_ID,
	/**
	 * Run an ID query, then run a query to fetch back all the entity data for those IDs
	 */
	ID_THEN_QUERY_ENTITY,
	/**
	 * Run a query that returns a custom projection based on the fetch value of the WebQuery<br />
	 * The result from the database <strong>will</strong> be an Object[] with the first few elements the same as fetch. N.B. the Object[] may be larger than the number of fetched columns (if, for example, the system had to add selections to satisfy SQL limitations - the most obvious case being for ORDER BYs)
	 */
	CUSTOM_PROJECTION;


	/**
	 * Returns true if this strategy will output full entities at the end of the process
	 *
	 * @return
	 */
	public boolean isReturningFullEntity()
	{
		return this == AUTO || this == ENTITY || this == ID_THEN_QUERY_ENTITY;
	}
}
