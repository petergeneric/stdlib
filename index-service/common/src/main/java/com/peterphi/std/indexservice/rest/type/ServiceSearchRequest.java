package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceSearchRequest
{
	/**
	 * The interface the service exposes
	 */
	@XmlAttribute(name = "interface", required = false)
	public String iface;

    @XmlElement(required = false)
    public PropertyList properties = new PropertyList();
}