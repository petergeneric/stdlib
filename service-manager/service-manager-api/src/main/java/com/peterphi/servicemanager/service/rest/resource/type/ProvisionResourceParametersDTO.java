package com.peterphi.servicemanager.service.rest.resource.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "ProvisionResourceParameters")
@XmlType(name = "ProvisionResourceParameters")
public class ProvisionResourceParametersDTO
{
	@XmlElementWrapper(name = "metadata")
	@XmlElement(name = "kvp")
	public List<ResourceKVP> metadata = new ArrayList<>(0);
}
