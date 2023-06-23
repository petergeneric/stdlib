package com.peterphi.std.guice.restclient.resteasy.impl.jaxb.fastinfoset;

import com.peterphi.std.util.DOMUtils;
import com.peterphi.std.util.jaxb.JAXBSerialiser;
import com.peterphi.std.util.jaxb.JAXBSerialiserFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class FastInfosetXmlRootElementProviderTest
{
	@XmlRootElement(name = "Test")
	public static class MyJaxbType
	{
		@XmlAttribute
		public String attr = "1";
		@XmlElement
		public String el = "2";

		@XmlElement
		public XMLBlockDTO xml;
	}

	@XmlType(name = "XmlBlock")
	@XmlRootElement(name = "xmlBlockDTO")
	@XmlAccessorType(XmlAccessType.PROPERTY)
	public static class XMLBlockDTO
	{
		public Element xml;


		public XMLBlockDTO()
		{
		}


		public XMLBlockDTO(final Document document)
		{
			this(document == null ? (Element) null : document.getDocumentElement());
		}


		public XMLBlockDTO(final Element xml)
		{
			setXML(xml);
		}


		@XmlAnyElement(lax = false)
		public Element getXML()
		{
			return this.xml;
		}


		public void setXML(final Element e)
		{
			this.xml = e;

			// Work around a MOXy bug whereby if an XmlAnyElement sets xmlns="", moxy emits a duplicate xmlns="" event when serialising with STaX
			if (e != null && Objects.equals("", this.xml.getAttribute("xmlns")))
			{
				this.xml.removeAttribute("xmlns");
			}
		}
	}
	@Test
	public void testSerialise()
	{
		JAXBSerialiserFactory fac = new JAXBSerialiserFactory(true);
		final JAXBSerialiser s = fac.getInstance(MyJaxbType.class);

		final MyJaxbType obj = new MyJaxbType();
		obj.xml = new XMLBlockDTO(DOMUtils.parse("<hello world=\"x\"></hello>"));


		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		FastInfosetXmlRootElementProvider.writeToOutputStream(s, obj, bos);
	}
}
