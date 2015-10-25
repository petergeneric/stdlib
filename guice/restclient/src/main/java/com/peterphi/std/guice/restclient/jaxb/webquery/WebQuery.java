package com.peterphi.std.guice.restclient.jaxb.webquery;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraints
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WebQuery add(final WQConstraintLine line)
	{
		this.constraints.constraints.add(line);

		return this;
	}


	public WebQuery eq(final String field, final Object value)
	{
		return add(WQConstraint.eq(field, value));
	}


	public WebQuery neq(final String field, final Object value)
	{
		return add(WQConstraint.neq(field, value));
	}


	public WebQuery isNull(final String field)
	{
		return add(WQConstraint.isNull(field));
	}


	public WebQuery isNotNull(final String field)
	{
		return add(WQConstraint.isNotNull(field));
	}


	public WebQuery lt(final String field, final Object value)
	{
		return add(WQConstraint.lt(field, value));
	}


	public WebQuery le(final String field, final Object value)
	{
		return add(WQConstraint.le(field, value));
	}


	public WebQuery gt(final String field, final Object value)
	{
		return add(WQConstraint.gt(field, value));
	}


	public WebQuery ge(final String field, final Object value)
	{
		return add(WQConstraint.ge(field, value));
	}


	public WebQuery contains(final String field, final Object value)
	{
		return add(WQConstraint.contains(field, value));
	}


	public WebQuery startsWith(final String field, final Object value)
	{
		return add(WQConstraint.startsWith(field, value));
	}


	public WebQuery range(final String field, final Object from, final Object to)
	{
		return add(WQConstraint.range(field, from, to));
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Sub-groups
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Construct a new AND group and return it for method chaining
	 *
	 * @return
	 */
	public WQGroup and()
	{
		final WQGroup and = WQGroup.newAnd();

		add(and);

		return and;
	}


	/**
	 * Construct a new OR group and return it for method chaining
	 *
	 * @return
	 */
	public WQGroup or()
	{
		final WQGroup and = WQGroup.newOr();

		add(and);

		return and;
	}


	/**
	 * Construct a new AND group, using the supplier to add the constraints to the group. Returns the original {@link WebQuery}
	 * for method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WebQuery and(Consumer<WQGroup> consumer)
	{
		final WQGroup and = and();

		add(and);

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(and);

		return this;
	}


	/**
	 * Construct a new OR group, using the supplier to add the constraints to the group. Returns the original {@link WebQuery}
	 * for
	 * method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WebQuery or(Consumer<WQGroup> consumer)
	{
		final WQGroup or = or();

		add(or);

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(or);

		return this;
	}
}
