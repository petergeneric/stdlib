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
}
