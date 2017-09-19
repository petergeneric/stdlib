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
	ID_THEN_QUERY_ENTITY;


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
