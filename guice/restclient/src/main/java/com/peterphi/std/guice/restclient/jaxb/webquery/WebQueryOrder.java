package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Defines a sort instruction
 */
@XmlType(name = "OrderType")
public class WebQueryOrder
{
	@XmlAttribute
	public String field;

	/**
	 * ASC or DESC
	 */
	@XmlAttribute
	public String direction;


	public WebQueryOrder()
	{
	}


	public WebQueryOrder(final String field, final String direction)
	{
		if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))
			throw new IllegalArgumentException("Direction must be asc or desc, not " + direction);

		this.field = field;
		this.direction = direction.toLowerCase();
	}


	public boolean isAsc()
	{
		return this.direction.equalsIgnoreCase("asc");
	}


	public String toLegacyForm()
	{
		return field + " " + direction;
	}


	@Override
	public String toString()
	{
		return "WebQueryOrder{" +
		       "field='" + field + '\'' +
		       ", direction='" + direction + '\'' +
		       '}';
	}


	public static WebQueryOrder asc(final String field)
	{
		return new WebQueryOrder(field, "asc");
	}


	public static WebQueryOrder desc(final String field)
	{
		return new WebQueryOrder(field, "desc");
	}


	public static WebQueryOrder parseLegacy(final String expr)
	{
		final String[] parts = expr.split(" ", 2);

		return new WebQueryOrder(parts[0], parts[1]);
	}
}
