package com.peterphi.std.guice.metrics.rest.types;

import org.jboss.resteasy.annotations.providers.jaxb.json.Mapped;
import org.jboss.resteasy.annotations.providers.jaxb.json.XmlNsMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "MetricsDocument")
@XmlType(name = "MetricsDocumentType")
@Mapped(namespaceMap = {@XmlNsMap(namespace = "http://ns.peterphi.com/stdlib/rest/metrics", jsonName = "")})
public class MetricsDocument
{
	@XmlElement(name = "counter")
	public List<MetricsCounter> counters = new ArrayList<>();
	@XmlElement(name = "gauge")
	public List<MetricsGauge> gauges = new ArrayList<>();
	@XmlElement(name = "histogram")
	public List<MetricsHistogram> histograms = new ArrayList<>();
	@XmlElement(name = "meter")
	public List<MetricsMeter> meters = new ArrayList<>();
}
