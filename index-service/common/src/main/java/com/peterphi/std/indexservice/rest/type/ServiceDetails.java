package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "serviceDetails")
public class ServiceDetails
{
	/**
	 * The fully qualified interface this service implements
	 */
	@XmlAttribute(name = "interface", required = true)
	public String iface;

	/**
	 * The endpoint to the base address of the service interface (for most services this is the /rest path in the webapp)
	 */
	@XmlAttribute(required = true)
	public String endpoint;

	/**
	 * Service properties
	 */
	@XmlElement(required = true)
	public PropertyList properties = new PropertyList();
}
