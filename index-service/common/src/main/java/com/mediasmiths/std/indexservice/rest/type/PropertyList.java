package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlType
public class PropertyList
{
	@XmlElement(name = "property")
	public List<PropertyValue> properties = new ArrayList<PropertyValue>();

	public PropertyList()
	{
	}

	public PropertyList(Map<String, String> map)
	{
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			properties.add(new PropertyValue(entry.getKey(), entry.getValue()));
		}
	}
}
