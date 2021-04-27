package com.peterphi.std.guice.restclient.jaxb.webquery;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlType(name = "ConstraintsType")
public class WQConstraints implements ConstraintContainer<WQConstraints>
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
	public WQConstraints add(final WQConstraintLine line)
	{
		if (line != null)
			constraints.add(line);

		return this;
	}


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
		if (constraints.size() != 1)
		{
			return constraints.stream().map(WQConstraintLine :: toQueryFragment).collect(Collectors.joining(" AND "));
		}
		else
		{
			return constraints.get(0).toQueryFragment();
		}
	}
}
