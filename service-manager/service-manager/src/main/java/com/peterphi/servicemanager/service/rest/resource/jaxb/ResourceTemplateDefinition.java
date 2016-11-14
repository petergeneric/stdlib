package com.peterphi.servicemanager.service.rest.resource.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Template")
public class ResourceTemplateDefinition
{
	/**
	 * If specified, this template is an existing Azure VM which will be started/stopped
	 */
	@XmlElement(required = false)
	public AzureExistingVM azureExistingVM;
}
