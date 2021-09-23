package com.peterphi.std.util.jaxb.pkg2;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement
public class SomeOtherXml
{
	@XmlAnyElement(lax = false)
	public Element someXmlBlock;
}
