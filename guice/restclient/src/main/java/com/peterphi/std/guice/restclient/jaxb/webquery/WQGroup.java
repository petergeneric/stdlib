package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

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


	public WQGroup add(WQConstraintLine line)
	{
		constraints.add(line);
		return this;
	}


	public WQGroup eq(final String field, final Object value)
	{
		constraints.add(WQConstraint.eq(field, value));
		return this;
	}


	public WQGroup neq(final String field, final Object value)
	{
		constraints.add(WQConstraint.neq(field, value));
		return this;
	}


	public WQGroup isNull(final String field)
	{
		constraints.add(WQConstraint.isNull(field));
		return this;
	}


	public WQGroup isNotNull(final String field)
	{
		constraints.add(WQConstraint.isNotNull(field));
		return this;
	}


	public WQGroup lt(final String field, final Object value)
	{
		constraints.add(WQConstraint.lt(field, value));
		return this;
	}


	public WQGroup le(final String field, final Object value)
	{
		constraints.add(WQConstraint.le(field, value));
		return this;
	}


	public WQGroup gt(final String field, final Object value)
	{
		constraints.add(WQConstraint.gt(field, value));
		return this;
	}


	public WQGroup ge(final String field, final Object value)
	{
		constraints.add(WQConstraint.ge(field, value));
		return this;
	}


	public WQGroup contains(final String field, final Object value)
	{
		constraints.add(WQConstraint.contains(field, value));
		return this;
	}


	public WQGroup startsWith(final String field, final Object value)
	{
		constraints.add(WQConstraint.startsWith(field, value));
		return this;
	}


	public WQGroup range(final String field, final Object from, final Object to)
	{
		constraints.add(WQConstraint.range(field, from, to));
		return this;
	}


	/**
	 * Construct a new empty AND group
	 *
	 * @return
	 */
	public static WQGroup and()
	{
		return new WQGroup(WQGroupType.AND);
	}


	/**
	 * Construct a new empty OR group
	 *
	 * @return
	 */
	public static WQGroup or()
	{
		return new WQGroup(WQGroupType.OR);
	}
}
