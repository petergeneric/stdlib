package com.peterphi.servicemanager.service.rest.resource.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AzureExistingVMType")
public class AzureExistingVM
{
	@XmlAttribute(name = "id", required = true)
	public String id;
}
