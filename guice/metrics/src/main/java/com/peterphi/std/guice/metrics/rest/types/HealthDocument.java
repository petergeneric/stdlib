package com.peterphi.std.guice.metrics.rest.types;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "HealthCheckDocument")
@XmlType(name = "HealthCheckDocumentType")
@BadgerFish
public class HealthDocument
{
	public List<HealthCheckResult> results = new ArrayList<>();
}
