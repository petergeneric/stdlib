package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="serviceHeartbeatRequest")
public class ServiceHeartbeatRequest
{
	@XmlAttribute(required = true)
	public String applicationId;
}
