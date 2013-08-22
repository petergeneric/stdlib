package com.mediasmiths.std.guice.restclient.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://ns.mediasmithsforge.com/stdlib/rest/exception", name = "exception")
public class ExceptionInfo
{
	@XmlElement(required = true, name = "shortName")
	public String shortName;

	@XmlElement(required = false, name = "detail")
	public String detail;

	@XmlElement(required = true, name = "className")
	public String className;

	@XmlElement(required = false, name = "stackTrace")
	public String stackTrace;

	@XmlElement(required = false, name = "cause")
	public ExceptionInfo causedBy;

	@XmlAnyElement
	public List<JAXBElement<?>> any = new ArrayList<JAXBElement<?>>();
}
