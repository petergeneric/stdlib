package com.peterphi.std.guice.restclient.resteasy.impl.jaxb.fastinfoset;

import com.peterphi.std.util.DOMUtils;
import com.peterphi.std.util.jaxb.JAXBSerialiser;
import com.sun.xml.fastinfoset.dom.DOMDocumentParser;
import com.sun.xml.fastinfoset.dom.DOMDocumentSerializer;
import com.sun.xml.fastinfoset.stax.factory.StAXInputFactory;
import com.sun.xml.fastinfoset.stax.factory.StAXOutputFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FastInfosetHelper
{
	private static com.sun.xml.fastinfoset.stax.factory.StAXInputFactory XML_INPUT_FACTORY;
	private static com.sun.xml.fastinfoset.stax.factory.StAXOutputFactory XML_OUTPUT_FACTORY;


	private static void init()
	{
		if (XML_INPUT_FACTORY == null)
		{
			com.sun.xml.fastinfoset.stax.factory.StAXInputFactory xif = new StAXInputFactory();
			final StAXOutputFactory xof = new StAXOutputFactory();
			// disable external entities
			xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
			// This disables DTDs entirely for that factory
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			// N.B. fastinfoset does not support setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,""));

			XML_OUTPUT_FACTORY = xof;
			XML_INPUT_FACTORY = xif;
		}


	}


	public static StAXInputFactory getInputFactory()
	{
		init();

		return XML_INPUT_FACTORY;
	}


	public static StAXOutputFactory getOutputFactory()
	{
		init();

		return XML_OUTPUT_FACTORY;
	}


	/**
	 * Decode FastInfoset binary into a DOM element
	 * @param bytes
	 * @return
	 */
	public static Document convert(final byte[] bytes)
	{
		if (bytes == null)
			return null;

		final var doc = DOMUtils.createDocumentBuilder().newDocument();
		try
		{
			DOMDocumentParser parser = new DOMDocumentParser();

			parser.parse(doc, new ByteArrayInputStream(bytes));
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error parsing Fast Infoset XML!", e);
		}
		return doc;
	}


	/**
	 * Convert a DOM Element into FastInfoset binary
	 * @param node
	 * @return
	 */
	public static byte[] convert(final Element node)
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
			DOMDocumentSerializer serialiser = new DOMDocumentSerializer();
			serialiser.setOutputStream(bos);
			serialiser.serialize(node);
			return bos.toByteArray();
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error encoding Fast Infoset XML!", e);
		}
	}


	public static Object deserialise(final byte[] bytes, final JAXBSerialiser serialiser)
	{
		try
		{
			final XMLStreamReader reader = getInputFactory().createXMLStreamReader(new ByteArrayInputStream(bytes));
			try
			{
				final Object obj = serialiser.deserialise(reader);

				if (obj == null)
					throw new RuntimeException("Malformed XML! JAXB returned null");
				else
					return obj;
			}
			finally
			{
				reader.close();
			}
		}
		catch (XMLStreamException e)
		{
			throw new RuntimeException("Unable to parse FastInfoset from bytes! " + e.getMessage(), e);
		}
	}


	public static byte[] serialise(final Object object, final JAXBSerialiser serialiser)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

		try
		{
			XMLStreamWriter writer = getOutputFactory().createXMLStreamWriter(bos);
			try
			{
				serialiser.serialise(object, writer);

				return bos.toByteArray();
			}
			finally
			{
				writer.close();
			}
		}
		catch (XMLStreamException e)
		{
			throw new RuntimeException("Unable to output FastInfoset to bytes! " + e.getMessage(), e);
		}
	}
}
