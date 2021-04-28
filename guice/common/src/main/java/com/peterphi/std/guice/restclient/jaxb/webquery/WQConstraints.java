package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "ConstraintsType")
public class WQConstraints
{
	@XmlAttribute
	public int offset = 0;
	@XmlAttribute
	public int limit = 0;
	@XmlAttribute
	public boolean computeSize = false;

	/**
	 * The subclass to constrain the results to
	 */
	@XmlAttribute(name = "subclass", required = false)
	public String subclass = null;

	/**
	 * The constraints. This group will be implicitly ANDed together
	 */
	@XmlElementRefs({@XmlElementRef(type = WQConstraint.class), @XmlElementRef(type = WQGroup.class)})
	public List<WQConstraintLine> constraints = new ArrayList<>();


	@Override
	public String toString()
	{
		return "WebQueryConstraints{" +
		       "offset=" +
		       offset +
		       ", limit=" +
		       limit +
		       ", computeSize=" +
		       computeSize +
		       ", clazz='" +
		       subclass +
		       '\'' +
		       ", constraints=" +
		       constraints +
		       '}';
	}


	public String toQueryFragment()
	{
		StringBuilder sb = new StringBuilder();

		toQueryFragment(sb);

		return sb.toString();
	}


	public void toQueryFragment(StringBuilder sb)
	{
		boolean first = true;
		for (WQConstraintLine constraint : constraints)
		{
			if (!first)
				sb.append("\nAND ");
			else
				first = false;

			constraint.toQueryFragment(sb);
		}
	}
}
