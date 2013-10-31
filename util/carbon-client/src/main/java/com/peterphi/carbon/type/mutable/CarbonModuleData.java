package com.peterphi.carbon.type.mutable;

import com.peterphi.carbon.type.XMLWrapper;
import org.jdom2.Element;

public class CarbonModuleData extends XMLWrapper
{
	public CarbonModuleData(Element element)
	{
		super(element);
	}

	public void setAttribute(String name, String value)
	{
		element.setAttribute(name, value);
	}

	public String getAttribute(String name)
	{
		return element.getAttributeValue(name);
	}
}
