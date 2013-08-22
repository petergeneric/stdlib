package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;

public class ServiceSearchRequest
{
	/**
	 * The interface the service exposes
	 */
	@XmlAttribute(name = "interface", required = false)
	public String iface;
}
