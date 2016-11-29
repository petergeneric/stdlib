package com.peterphi.servicemanager.service.rest.resource.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "HostnameResponse")
public class HostnameResponseDTO
{
	@XmlElement(required = true)
	public String primaryHostname;

	@XmlElementWrapper(name = "alternateHostnames")
	@XmlElement(name = "hostname")
	public List<String> alternateHostnames = new ArrayList<>();

	@XmlElement(required = false)
	public String sslKeypair;
	@XmlElement(required = false)
	public String sslCert;
	@XmlElement(required = false)
	public String sslChain;
}
