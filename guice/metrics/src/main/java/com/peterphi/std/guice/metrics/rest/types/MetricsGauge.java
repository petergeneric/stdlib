package com.peterphi.std.guice.metrics.rest.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Gauge")
@XmlType(name = "GaugeType")
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
