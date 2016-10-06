package com.peterphi.servicemanager.service.rest.resource.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "TemplateInstance")
public class ResourceInstanceDTO
{
	@XmlAttribute(required = true)
	public int id;

	@XmlElement(required = true)
	public String templateName;

	@XmlElement(required = true)
	public String provider;
	@XmlElement(required = false)
	public String providerInstanceId;
	@XmlElement(required = true)
	public ResourceInstanceState state;

	@XmlElement(required = true)
	public ProvisionResourceParametersDTO parameters;

	@XmlElement(required = true)
	public Date created;
	@XmlElement(required = true)
	public Date updated;
}
