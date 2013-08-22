package com.mediasmiths.carbon.type.mutable;

import com.mediasmiths.carbon.type.CarbonXMLWrapper;
import org.jdom2.Element;

public class CarbonModuleData extends CarbonXMLWrapper
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
