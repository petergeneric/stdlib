package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@XmlType
public class ServiceDescription
{
	/**
	 * The unique id for the containing application, of the form [A-Za-z0-9\-_]+ (case sensitive)
	 */
	@XmlAttribute(required = true)
	public String applicationId;

	/**
	 * The application name (if present)
	 */
	@XmlAttribute(required = false)
	public String applicationName;

	@XmlElement(required = true)
	public ServiceDetails details;

	@XmlAttribute(required = true)
	public Date lastHeartbeat;

	@XmlAttribute(required = true)
	public Date nextHeartbeatDue;

	public ServiceDescription()
	{
	}

	public ServiceDescription(final String applicationId, final String applicationName, final ServiceDetails details)
	{
		this.applicationId = applicationId;
		this.applicationName = applicationName;
		this.details = details;
	}
}
