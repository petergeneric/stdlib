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
public class WebQuery
{
	/**
	 * What to fetch - the entity or the primary key
	 */
	@XmlAttribute
	public String fetch = "entity";
	/**
	 * What relationships to expand (by default, all relationships are expanded)
	 */
	@XmlAttribute
	public String expand = "all";

	@XmlElement
	public WQConstraints constraints = new WQConstraints();

	@XmlElementWrapper(name = "ordering")
	@XmlElement(name = "order")
	public List<WQOrder> orderings = new ArrayList<>();


	public WebQuery expand(String... relationships)
	{
		this.expand = String.join(",", relationships);

		return this;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Convenience getters
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public int getOffset()
	{
		return constraints.offset;
	}


	public int getLimit()
	{
		return constraints.limit;
	}


	public boolean isComputeSize()
	{
		return constraints.computeSize;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Builder methods
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery subclass(String... subclasses)
	{
		this.constraints.subclass = String.join(",", subclasses);

		return this;
	}


	public WebQuery fetch(final String expression)
	{
		this.fetch = expression;

		return this;
	}


	public WebQuery offset(final int offset)
	{
		constraints.offset = offset;

		return this;
	}


	public WebQuery limit(final int limit)
	{
		constraints.limit = limit;

		return this;
	}


	public WebQuery computeSize(final boolean computeSize)
	{
		constraints.computeSize = computeSize;

		return this;
	}


	public WebQuery eq(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.eq(field, value));
		return this;
	}


	public WebQuery neq(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.neq(field, value));
		return this;
	}


	public WebQuery isNull(final String field)
	{
		constraints.constraints.add(WQConstraint.isNull(field));
		return this;
	}


	public WebQuery isNotNull(final String field)
	{
		constraints.constraints.add(WQConstraint.isNotNull(field));
		return this;
	}


	public WebQuery lt(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.lt(field, value));
		return this;
	}


	public WebQuery le(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.le(field, value));
		return this;
	}


	public WebQuery gt(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.gt(field, value));
		return this;
	}


	public WebQuery ge(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.ge(field, value));
		return this;
	}


	public WebQuery contains(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.contains(field, value));
		return this;
	}


	public WebQuery startsWith(final String field, final Object value)
	{
		constraints.constraints.add(WQConstraint.startsWith(field, value));
		return this;
	}


	public WebQuery range(final String field, final Object from, final Object to)
	{
		constraints.constraints.add(WQConstraint.range(field, from, to));
		return this;
	}


	public WebQuery orderAsc(final String field)
	{
		orderings.add(WQOrder.asc(field));

		return this;
	}


	public WebQuery orderDesc(final String field)
	{
		orderings.add(WQOrder.desc(field));

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
