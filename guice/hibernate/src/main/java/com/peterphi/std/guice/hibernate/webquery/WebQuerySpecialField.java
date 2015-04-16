package com.peterphi.std.guice.hibernate.webquery;

/**
 * Special WebQuery fields
 */
public enum WebQuerySpecialField
{
	/**
	 * Set the index of the first result to return for this query
	 */
	OFFSET("_offset"),
	/**
	 * Set to the maximum results to return for this query
	 */
	LIMIT("_limit"),
	ORDER("_order"),
	/**
	 * Set to true to request the resultset size be computed
	 */
	COMPUTE_SIZE("_compute_size"),
	/**
	 * Set to some class to specify the subclass to return (for entities with type hierarchies)
	 */
	CLASS("_class");

	private final String name;


	WebQuerySpecialField(final String name)
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


	public WebQuerySpecialField getByName(final String fieldName)
	{
		for (WebQuerySpecialField field : values())
		{
			if (field.name.equals(fieldName))
				return field;
		}

		throw new IllegalArgumentException("No core WebQueryField with name: " + fieldName);
	}
}
