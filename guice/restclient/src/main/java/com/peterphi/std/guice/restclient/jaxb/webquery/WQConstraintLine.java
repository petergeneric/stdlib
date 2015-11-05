package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlType;

/**
 * Base class for components of a Constraint Group
 */
@XmlType(name = "ConstraintLineType")
public abstract class WQConstraintLine
{
	public abstract String toQueryFragment();
}
