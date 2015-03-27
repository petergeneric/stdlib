package com.peterphi.std.indexservice.rest.type;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "applicationSearchResults")
public class ApplicationSearchResults
{
	@XmlElement(name = "applicationId")
	public List<String> ids = new ArrayList<String>();
}