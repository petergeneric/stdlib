package com.peterphi.servicemanager.hostagent.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeployUpdatedCerts")
public class DeployUpdatedCerts
{
	@XmlElement(required = true)
	public String managementToken;

	@XmlElement(required = false)
	public String sslKeypair;
	@XmlElement(required = false)
	public String sslCert;
	@XmlElement(required = false)
	public String sslChain;
}
