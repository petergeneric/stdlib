package com.peterphi.std.guice.xmltesting;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;

/**
 * Filters XML documents to remove nodes that should not be sent to XMLUnit
 */
class XMLTestInputFilter
{
	private Set<String> attributesToRemove = new HashSet<>();
	private boolean removeComments = false;


	public XMLTestInputFilter()
	{
	}


	public Set<String> getAttributesToRemove()
	{
		return attributesToRemove;
	}


	public void setAttributesToRemove(final Set<String> attributesToRemove)
	{
		this.attributesToRemove = attributesToRemove;
	}


	public boolean isRemoveComments()
	{
		return removeComments;
	}


	public void setRemoveComments(final boolean removeComments)
	{
		this.removeComments = removeComments;
	}


	public void apply(final Document doc)
	{
		apply(doc.getDocumentElement());
	}


	void apply(NodeList list)
	{
		for (int i = 0; i < list.getLength(); i++)
		{
			apply(list.item(i));
		}
	}


	void apply(Node node)
	{
		if (removeComments && node instanceof org.w3c.dom.Comment)
			node.getParentNode().removeChild(node);
		else
		{
			apply(node.getChildNodes());

			apply(node.getAttributes());
		}
	}


	private void apply(final NamedNodeMap map)
	{
		if (map == null)
			return;

		Attr[] attrs = new Attr[map.getLength()];
		for (int i = 0; i < map.getLength(); i++)
			attrs[i] = (Attr) map.item(i);

		for (Attr attr : attrs)
		{
			if (attributesToRemove.contains(attr.getLocalName()))
				map.removeNamedItem(attr.getName());
		}
	}
}
