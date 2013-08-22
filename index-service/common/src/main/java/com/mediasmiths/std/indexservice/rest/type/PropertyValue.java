package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class PropertyValue
{
	@XmlAttribute
	public String name;

	@XmlAttribute
	public String value;

	public PropertyValue()
	{
	}

	public PropertyValue(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
}
