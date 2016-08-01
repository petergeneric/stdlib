package com.peterphi.std.guice.restclient.jaxb.webqueryschema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "EntityType")
public class WQEntitySchema
{
	@XmlAttribute
	public String name;

	@XmlElementWrapper(name = "childEntities")
	@XmlElement(name = "entity")
	public List<String> childEntityNames;

	@XmlAttribute
	public String discriminator;

	@XmlElementWrapper(name = "properties")
	@XmlElement(name = "property")
	public List<WQEntityProperty> properties = new ArrayList<>();
}
