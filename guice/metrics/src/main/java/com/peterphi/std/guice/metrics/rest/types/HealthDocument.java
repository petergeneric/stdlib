package com.peterphi.std.guice.metrics.rest.types;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "HealthCheckDocument")
@XmlType(name = "HealthCheckDocumentType")
@Mapped(namespaceMap = {@XmlNsMap(namespace = "http://ns.peterphi.com/stdlib/rest/metrics", jsonName = "")})
public class HealthDocument
{
	public List<HealthCheckResult> results = new ArrayList<>();
}
