package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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


	public WQGroup(final WQGroupType operator, final List<WQConstraintLine> constraints)
	{
		this(operator);

		this.constraints.addAll(constraints);
	}


	@Override
	public String toString()
	{
		return "WQGroup{" + operator +
		       ", constraints=" + constraints +
		       "} ";
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constraints
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public WQGroup add(WQConstraintLine line)
	{
		constraints.add(line);
		return this;
	}


	/**
	 * Assert that a field equals one of the provided values. Implicitly creates a new OR group if multiple values are supplied
	 *
	 * @param field
	 * @param values
	 *
	 * @return
	 */
	public WQGroup eq(final String field, final Object... values)
	{
		if (values == null)
		{
			add(WQConstraint.eq(field, null));
		}
		else if (values.length == 1)
		{
			add(WQConstraint.eq(field, values[0]));
		}
		else if (values.length > 1)
		{
			final WQGroup or = or();

			for (Object value : values)
				or.eq(field, value);
		}

		return this;
	}
	
	/**
	 * Assert that a field equals one of the provided values. Implicitly creates a new OR group if multiple values are supplied.
	 * At least one value must be supplied.
	 *
	 * @param field
	 * @param values
	 *
	 * @return
	 */
	public WQGroup eq(final String field, final Collection<?> values)
	{
		if (values == null)
			throw new IllegalArgumentException("Must supply at least one value to .eq when passing a Collection");
		else if (values.size() == 0)
			return eq(field, values.stream().findFirst().get());
		else
		{
			final WQGroup or = or();

			for (Object value : values)
				or.eq(field, value);

			return this;
		}
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
	 * Construct a new OR group and return it for method chaining
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
		final WQGroup or = WQGroup.newOr();

		add(or);

		return or;
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


	@Override
	public String toQueryFragment()
	{
		if (constraints.size() == 1)
			return constraints.get(0).toQueryFragment();
		else
		{
			final String operatorStr = " " + operator.name() + " ";

			return constraints.stream().map(WQConstraintLine:: toQueryFragment).collect(Collectors.joining(operatorStr,
			                                                                                               "(",
			                                                                                               ")"));
		}
	}
}
