package com.mediasmiths.std.config.values.xml;

import java.util.*;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

class XMLElement {
	private final XMLElement parent;
	private final String name;
	private final String value;
	private final Map<String, List<XMLElement>> children = new HashMap<String, List<XMLElement>>();

	public XMLElement(Document d) {
		this(null, d.getDocumentElement());
	}


	public XMLElement(XMLElement parent, Element element) {
		this.parent = parent;
		this.name = element.getLocalName();

		// If possible, set the text value
		if (element.getFirstChild() != null && element.getFirstChild() instanceof Text) {
			this.value = element.getTextContent();
		}
		else {
			this.value = null;
		}

		// Parse attributes
		final NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			final Attr attr = (Attr) attrs.item(i);

			XMLElement parsed = new XMLElement(this, attr.getName(), attr.getValue());
			addChild(parsed);
		}

		// Parse child nodes

		int childElements = 0;
		for (Node childNode : children(element)) {
			if (childNode instanceof Element) {
				final Element childElement = (Element) childNode;
				childElements++;

				final XMLElement parsed = new XMLElement(this, childElement);
				addChild(parsed);
			}
		}

		if (this.value != null && childElements > 0)
			throw new IllegalArgumentException("Element " + this + " has Text value but also has " + childElements + " Child Elements!");
	}


	public static List<Node> children(Node parent) {
		NodeList nodes = parent.getChildNodes();

		final int length = nodes.getLength();

		if (length == 0)
			return Collections.emptyList();
		else if (length == 1)
			return Collections.singletonList(nodes.item(0));
		else {
			final List<Node> children = new ArrayList<Node>(length);

			for (int i = 0; i < length; i++) {
				children.add(nodes.item(i));
			}

			return children;
		}
	}


	/**
	 * Strips whitespace from a tree but does not touch any CDATA segment
	 * 
	 * @param from
	 */
	public static void stripWhitespace(final Node from) {
		if (from instanceof Text && !(from instanceof CDATASection)) {
			final Text text = (Text) from;

			final String trimmed = (text.getData() == null) ? "" : text.getData().trim();
			if (text.isElementContentWhitespace() || trimmed.isEmpty()) {
				from.getParentNode().removeChild(from);
				return;
			}
			else {
				text.setData(trimmed);
			}
		}
		else {
			for (Node child : children(from)) {
				stripWhitespace(child);
			}
		}
	}


	public XMLElement(XMLElement parent, String name, String value) {
		this.parent = parent;
		this.name = name;
		this.value = value;
	}


	public void addChild(XMLElement element) {
		List<XMLElement> siblings = getChildren(element.name);

		if (siblings == null) {
			siblings = new ArrayList<XMLElement>(1);
			children.put(element.name, siblings);
		}

		siblings.add(element);
	}


	public List<XMLElement> getChildren(String name) {
		if (children.containsKey(name)) {
			return children.get(name);
		}
		else
			return null;
	}


	public String getName() {
		return this.name;
	}


	public String getValue() {
		if (value == null)
			throw new IllegalArgumentException("Not a Text element: " + name);
		else
			return value;
	}

	@Override
	public String toString() {
		if (parent != null)
			return parent.getName() + "/" + getName();
		else
			return "/" + getName();
	}
}
