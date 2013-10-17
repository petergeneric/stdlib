package com.peterphi.carbon.type;

import org.jdom2.Element;

/**
 * Generic wrapper around some Carbon XML element
 */
public class CarbonXMLWrapper
{
	public Element element;

	public CarbonXMLWrapper(Element element)
	{
		this.element = element;
	}

	/**
	 * Return the XML Element this object is wrapping
	 *
	 * @return
	 */
	public Element getElement()
	{
		return element;
	}

	/**
	 * Helper method to set the value of an attribute on this Element
	 *
	 * @param name
	 * 		the attribute name
	 * @param value
	 * 		the value to set - if null the attribute is removed
	 */
	public void setAttribute(String name, String value)
	{
		if (value != null)
			getElement().setAttribute(name, value);
		else
			getElement().removeAttribute(name);
	}

	/**
	 * Helper method to retrieve the value of an attribute on this Element
	 *
	 * @param name
	 *
	 * @return
	 */
	public String getAttribute(String name)
	{
		return getElement().getAttributeValue(name);
	}
}
