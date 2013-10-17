package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="registrationRequest")
public class RegistrationRequest
{

	/**
	 * The application name (if present)
	 */
	@XmlAttribute(required = false)
	public String applicationName;

	@XmlElement(name = "service")
	public List<ServiceDetails> services = new ArrayList<ServiceDetails>();
}
