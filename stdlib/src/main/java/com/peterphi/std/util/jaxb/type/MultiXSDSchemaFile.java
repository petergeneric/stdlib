package com.peterphi.std.util.jaxb.type;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "SchemaFileType")
public class MultiXSDSchemaFile
{
	/**
	 * The filename
	 */
	@XmlAttribute(name = "name", required = true)
	public String name;

	/**
	 * The XML Schema (should be an Element)
	 */
	@XmlElement(namespace = "http://www.w3.org/2001/XMLSchema", name = "schema", required = true)
	public Object schema;


	public Element schemaElement()
	{
		return (Element) schema;
	}


	@Override
	public String toString()
	{
		return "MultiXSDSchemaFile{" + name + "}";
	}
}
