package com.peterphi.servicemanager.service.rest.resource.type;

import com.peterphi.std.util.jaxb.JAXBSerialiser;

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
	public List<ResourceKVP> metadata = new ArrayList(0);


	public static void main(String[] args) throws Exception
	{
		System.out.println(JAXBSerialiser.getInstance(ProvisionResourceParametersDTO.class)
		                                 .serialise(new ProvisionResourceParametersDTO()));
	}
}
