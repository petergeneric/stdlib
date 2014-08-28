package com.peterphi.std.guice.metrics.rest.types;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Gauge")
@XmlType(name = "GaugeType")
@BadgerFish
public class MetricsGauge
{
	@XmlElement
	public String name;
	@XmlElement
	public String value;


	public MetricsGauge()
	{
	}


	public MetricsGauge(final String name, final String value)
	{
		this.name = name;
		this.value = value;
	}
}
