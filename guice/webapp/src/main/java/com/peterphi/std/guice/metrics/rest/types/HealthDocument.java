package com.peterphi.std.guice.metrics.rest.types;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "HealthCheckDocument")
@XmlType(name = "HealthCheckDocumentType")
public class HealthDocument
{
	public List<HealthCheckResult> results = new ArrayList<>();
}
