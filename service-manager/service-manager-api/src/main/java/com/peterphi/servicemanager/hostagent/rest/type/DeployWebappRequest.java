package com.peterphi.servicemanager.hostagent.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeployWebappRequest")
public class DeployWebappRequest
{
	@XmlElement(required = true)
	public String managementToken;

	/**
	 * The name of the tomcat domain
	 */
	@XmlElement(required = true)
	public String container;

	/**
	 * The name to use for the webapp when deployed locally
	 */
	@XmlElement(required = true)
	public String name;

	/**
	 * HTTP source: endpoint to issue a GET to
	 */
	@XmlElement(required = false)
	public String httpEndpoint;

	/**
	 * Service Manager source: resource name
	 */
	@XmlElement(required = false)
	public String resourceName;

	/**
	 * Service Manager source: resource version
	 */
	@XmlElement(required = false)
	public String resourceVersion;

	/**
	 * The expected SHA256 of the resource (optional)
	 */
	@XmlElement(required = false)
	public String sha256;
}
