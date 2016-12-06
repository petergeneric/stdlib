package com.peterphi.servicemanager.service.rest.resource.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "HostnameResponse")
public class HostnameResponseDTO
{
	@XmlElement(required = true)
	public String primaryHostname;

	@XmlElementWrapper(name = "alternateHostnames")
	@XmlElement(name = "hostname")
	public List<String> alternateHostnames = new ArrayList<>();

	/**
	 * A secret management token shared between the Service Manager and this host
	 */
	@XmlElement(required = true)
	public String managementToken;

	@XmlElement(required = false)
	public String sslKeypair;
	@XmlElement(required = false)
	public String sslCert;
	@XmlElement(required = false)
	public String sslChain;

	@XmlElement(required=true)
	public Date updated;
	@XmlElement(required=true)
	public Date created;
}
