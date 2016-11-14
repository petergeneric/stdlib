package com.peterphi.servicemanager.service.rest.resource.type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@XmlRootElement(name = "ResourceTemplate")
@XmlType(name = "ResourceTemplateType")
public class ResourceTemplateDTO
{
	@XmlAttribute(required = true)
	public String id;

	@XmlElement
	public String latestRevision;
	@XmlElement(required = true)
	public int revisions;

	@XmlElement
	public Date created;
	@XmlElement
	public Date updated;
}
