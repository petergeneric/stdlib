package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a group of constraints to be ANDed or ORred together
 */
@XmlRootElement(name = "ConstraintsGroup")
@XmlType(name = "ConstraintGroupType")
public class WQGroup extends WQConstraintLine implements ConstraintContainer<WQGroup>
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
		if (line != null)
			constraints.add(line);

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
