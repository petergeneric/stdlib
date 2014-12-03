package com.peterphi.std.guice.hibernate.webquery;

/**
 * Special WebQuery fields
 */
public enum WebQuerySpecialField
{
	OFFSET("_offset"),
	LIMIT("_limit"),
	ORDER("_order"),
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
