package com.mediasmiths.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="serviceSearchResults")
public class ServiceSearchResults
{
	@XmlElement(name = "service")
	public List<ServiceDescription> services = new ArrayList<ServiceDescription>();
}
