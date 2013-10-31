package com.peterphi.carbon.type;

import org.jdom2.Element;

import java.util.List;

/**
 * Generic wrapper around some Carbon XML element
 */
public class XMLWrapper
{
	public Element element;


	public XMLWrapper(Element element)
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


	/**
	 * Helper method to retrieve a child element with a particular name and index
	 *
	 * @param name
	 * 		the name of the element
	 * @param index
	 * 		the index (where 0 is the first element with that name)
	 *
	 * @return an Element (or null if none could be found by that name or with that index)
	 */
	protected Element getElement(String name, int index)
	{
		List<Element> children = getElement().getChildren(name);

		if (children.size() > index)
			return children.get(index);
		else
			return null;
	}
}
