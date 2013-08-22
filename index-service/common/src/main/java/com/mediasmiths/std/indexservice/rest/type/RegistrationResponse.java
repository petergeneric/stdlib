package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="registrationResponse")
public class RegistrationResponse
{
	@XmlAttribute(required = true)
	public String applicationId;

	public RegistrationResponse()
	{
	}

	public RegistrationResponse(String applicationId)
	{
		this.applicationId = applicationId;
	}
}
