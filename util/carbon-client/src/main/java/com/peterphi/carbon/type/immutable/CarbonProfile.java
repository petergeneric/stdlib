package com.peterphi.carbon.type.immutable;

import org.jdom2.Element;

public class CarbonProfile
{
	private Element element;

	public CarbonProfile(Element element)
	{
		this.element = element;
	}

	public String getName()
	{
		return element.getAttributeValue("Name");
	}

	public String getDescription()
	{
		return element.getAttributeValue("Description");
	}

	public String getCategory()
	{
		return element.getAttributeValue("Category");
	}

	public String getGUID()
	{
		return element.getAttributeValue("GUID");
	}
}
