package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a database query to be executed
 */
@BadgerFish
@XmlRootElement(name = "WebQueryDefinition")
@XmlType(name = "QueryDefinitionType")
public class WebQueryDefinition
{
	@XmlAttribute
	public String fetch = "entity";
	@XmlAttribute
	public String expand = "all";

	@XmlElement
	public WebQueryConstraints constraints = new WebQueryConstraints();

	@XmlElementWrapper(name = "ordering")
	@XmlElement(name = "order")
	public List<WebQueryOrder> orderings = new ArrayList<>();


	public WebQueryDefinition expand(String... relationships)
	{
		this.expand = String.join(",", relationships);

		return this;
	}


	public WebQueryDefinition subclass(String... subclasses)
	{
		this.constraints.subclass = String.join(",", subclasses);

		return this;
	}


	public WebQueryDefinition fetch(final String expression)
	{
		this.fetch = expression;

		return this;
	}


	public WebQueryDefinition offset(final int offset)
	{
		constraints.offset = offset;

		return this;
	}


	public WebQueryDefinition limit(final int limit)
	{
		constraints.limit = limit;

		return this;
	}


	public WebQueryDefinition computeSize(final boolean computeSize)
	{
		constraints.computeSize = computeSize;

		return this;
	}


	public WebQueryDefinition eq(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.eq(field, value));
		return this;
	}


	public WebQueryDefinition neq(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.neq(field, value));
		return this;
	}


	public WebQueryDefinition isNull(final String field)
	{
		constraints.constraints.add(WebQueryConstraint.isNull(field));
		return this;
	}


	public WebQueryDefinition isNotNull(final String field)
	{
		constraints.constraints.add(WebQueryConstraint.isNotNull(field));
		return this;
	}


	public WebQueryDefinition lt(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.lt(field, value));
		return this;
	}


	public WebQueryDefinition le(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.le(field, value));
		return this;
	}


	public WebQueryDefinition gt(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.gt(field, value));
		return this;
	}


	public WebQueryDefinition ge(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.ge(field, value));
		return this;
	}


	public WebQueryDefinition contains(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.contains(field, value));
		return this;
	}


	public WebQueryDefinition startsWith(final String field, final Object value)
	{
		constraints.constraints.add(WebQueryConstraint.startsWith(field, value));
		return this;
	}


	public WebQueryDefinition range(final String field, final Object from, final Object to)
	{
		constraints.constraints.add(WebQueryConstraint.range(field, from, to));
		return this;
	}


	public WebQueryDefinition orderAsc(final String field)
	{
		orderings.add(WebQueryOrder.asc(field));

		return this;
	}


	public WebQueryDefinition orderDesc(final String field)
	{
		orderings.add(WebQueryOrder.desc(field));

		return this;
	}


	@Override
	public String toString()
	{
		return "WebQueryDefinition{" +
		       "fetch='" + fetch + '\'' +
		       ", expand='" + expand + '\'' +
		       ", constraints=" + constraints +
		       ", orderings=" + orderings +
		       '}';
	}
}
