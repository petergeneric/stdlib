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
public class WebQueryConstraintGroup extends WebQueryConstraintLine
{
	@XmlAttribute(required = true)
	public WebQueryCombiningOperator operator;

	@XmlElementRefs({@XmlElementRef(name = "constraint", type = WebQueryConstraint.class),
	                 @XmlElementRef(name = "constraints", type = WebQueryConstraintGroup.class)})
	public List<WebQueryConstraintLine> constraints = new ArrayList<>();


	public WebQueryConstraintGroup()
	{
	}


	public WebQueryConstraintGroup(final WebQueryCombiningOperator operator)
	{
		this.operator = operator;
	}


	public WebQueryConstraintGroup add(WebQueryConstraintLine line)
	{
		constraints.add(line);
		return this;
	}


	public WebQueryConstraintGroup eq(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.eq(field, value));
		return this;
	}


	public WebQueryConstraintGroup neq(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.neq(field, value));
		return this;
	}


	public WebQueryConstraintGroup isNull(final String field)
	{
		constraints.add(WebQueryConstraint.isNull(field));
		return this;
	}


	public WebQueryConstraintGroup isNotNull(final String field)
	{
		constraints.add(WebQueryConstraint.isNotNull(field));
		return this;
	}


	public WebQueryConstraintGroup lt(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.lt(field, value));
		return this;
	}


	public WebQueryConstraintGroup le(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.le(field, value));
		return this;
	}


	public WebQueryConstraintGroup gt(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.gt(field, value));
		return this;
	}


	public WebQueryConstraintGroup ge(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.ge(field, value));
		return this;
	}


	public WebQueryConstraintGroup contains(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.contains(field, value));
		return this;
	}


	public WebQueryConstraintGroup startsWith(final String field, final Object value)
	{
		constraints.add(WebQueryConstraint.startsWith(field, value));
		return this;
	}


	public WebQueryConstraintGroup range(final String field, final Object from, final Object to)
	{
		constraints.add(WebQueryConstraint.range(field, from, to));
		return this;
	}


	/**
	 * Construct a new empty AND group
	 *
	 * @return
	 */
	public static WebQueryConstraintGroup and()
	{
		return new WebQueryConstraintGroup(WebQueryCombiningOperator.AND);
	}


	/**
	 * Construct a new empty OR group
	 *
	 * @return
	 */
	public static WebQueryConstraintGroup or()
	{
		return new WebQueryConstraintGroup(WebQueryCombiningOperator.OR);
	}
}
