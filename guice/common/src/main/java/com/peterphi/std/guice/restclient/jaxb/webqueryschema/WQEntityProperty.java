package com.peterphi.std.guice.restclient.jaxb.webqueryschema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name = "EntityPropertyType")
public class WQEntityProperty
{
	@XmlAttribute
	public String name;

	@XmlAttribute
	public WQDataType type;

	@XmlAttribute
	public String relation;

	@XmlAttribute
	public boolean nullable;

	/**
	 * If datatype is enum, the permitted values for this enum
	 */
	@XmlElementWrapper(name = "enumValues")
	@XmlElement(name = "val")
	public List<String> enumValues;
}
