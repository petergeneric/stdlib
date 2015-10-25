package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Defines a group of constraints to be ANDed or ORred together
 */
@XmlRootElement(name = "ConstraintsGroup")
@XmlType(name = "ConstraintGroupType")
public class WQGroup extends WQConstraintLine
{
	@XmlAttribute(required = true)
	public WQGroupType operator;

	@XmlElementRefs({@XmlElementRef(name = "constraint", type = WQConstraint.class),
	                 @XmlElementRef(name = "constraints", type = WQGroup.class)})
	public List<WQConstraintLine> constraints = new ArrayList<>();


	public WQGroup()
	{
	}


	public WQGroup(final WQGroupType operator)
	{
		this.operator = operator;
	}


	@Override
	public String toString()
	{
		return "WQGroup{" +
		       "operator=" + operator +
		       ", constraints=" + constraints +
		       "} " + super.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraints
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WQGroup add(WQConstraintLine line)
	{
		constraints.add(line);
		return this;
	}


	public WQGroup eq(final String field, final Object value)
	{
		return add(WQConstraint.eq(field, value));
	}


	public WQGroup neq(final String field, final Object value)
	{
		return add(WQConstraint.neq(field, value));
	}


	public WQGroup isNull(final String field)
	{
		return add(WQConstraint.isNull(field));
	}


	public WQGroup isNotNull(final String field)
	{
		return add(WQConstraint.isNotNull(field));
	}


	public WQGroup lt(final String field, final Object value)
	{
		return add(WQConstraint.lt(field, value));
	}


	public WQGroup le(final String field, final Object value)
	{
		return add(WQConstraint.le(field, value));
	}


	public WQGroup gt(final String field, final Object value)
	{
		return add(WQConstraint.gt(field, value));
	}


	public WQGroup ge(final String field, final Object value)
	{
		return add(WQConstraint.ge(field, value));
	}


	public WQGroup contains(final String field, final Object value)
	{
		return add(WQConstraint.contains(field, value));
	}


	public WQGroup startsWith(final String field, final Object value)
	{
		return add(WQConstraint.startsWith(field, value));
	}


	public WQGroup range(final String field, final Object from, final Object to)
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
		final WQGroup and = WQGroup.newAnd();

		add(and);

		return and;
	}


	/**
	 * Construct a new AND group, using the supplier to add the constraints to the group. Returns the original {@link WQGroup}
	 * for method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WQGroup and(Consumer<WQGroup> consumer)
	{
		final WQGroup and = WQGroup.newAnd();

		add(and);

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(and);

		return this;
	}


	/**
	 * Construct a new OR group, using the supplier to add the constraints to the group. Returns the original {@link WQGroup}
	 * for
	 * method chaining
	 *
	 * @param consumer
	 *
	 * @return
	 */
	public WQGroup or(Consumer<WQGroup> consumer)
	{
		final WQGroup or = WQGroup.newOr();

		add(or);

		// Let the consumer build their sub-constraints
		if (consumer != null)
			consumer.accept(or);

		return this;
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper constructors
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Construct a new empty AND group
	 *
	 * @return
	 */
	public static WQGroup newAnd()
	{
		return new WQGroup(WQGroupType.AND);
	}


	/**
	 * Construct a new empty OR group
	 *
	 * @return
	 */
	public static WQGroup newOr()
	{
		return new WQGroup(WQGroupType.OR);
	}
}
