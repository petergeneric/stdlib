package com.peterphi.servicemanager.hostagent.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeployWebappRequest")
public class DeployWebappRequest
{
	@XmlElement(required = true)
	public String managementToken;

	@XmlElement(required = true)
	public String container;

	@XmlElement(required = true)
	public String resourceName;

	@XmlElement(required = true)
	public String resourceVersion;
}
