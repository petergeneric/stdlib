package com.peterphi.servicemanager.service.rest.resource.type;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HostnameRequest")
public class HostnameRequestDTO
{
	@XmlElement(required = false)
	public String prefix = "host";

	/**
	 * Allows a specific hostname to be requested instead of requesting one be assigned
	 */
	@XmlElement(required = false)
	public String hostname;

	@XmlElement(required = true)
	public String ip;

	@XmlElement(required = false)
	public boolean ssl = false;


	@Override
	public String toString()
	{
		return Objects
				       .toStringHelper(this)
				       .add("prefix", prefix)
				       .add("hostname", hostname)
				       .add("ip", ip)
				       .add("ssl", ssl)
				       .toString();
	}
}
