package com.peterphi.std.guice.metrics.rest.types;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Counter")
@XmlType(name = "CounterType")
@BadgerFish
public class MetricsCounter
{
	@XmlElement
	public String name;
	@XmlElement
	public long count;


	public MetricsCounter()
	{
	}


	public MetricsCounter(final String name, final long count)
	{
		this.name = name;
		this.count = count;
	}
}
