/***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************
 * Performs utility XML operations Author: JMC Created: 10th April 2003
 **************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/

package com.mediasmiths.std.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class DOMUtils
{
	private static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;


	static
	{
		DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
		DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
	}


	private DOMUtils()
	{
	}


	/**
	 * Create a new (namespace-aware) DocumentBuilder
	 *
	 * @return
	 */
	public static DocumentBuilder createDocumentBuilder()
	{
		try
		{
			return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException("Error creating DocumentBuilder: " + e.getMessage(), e);
		}
	}


	public static Document parse(File file)
	{
		if (file == null)
			throw new IllegalArgumentException("Must provide non-null XML input to parse!");

		try
		{
			final Document document;

			DocumentBuilder documentBuilder = createDocumentBuilder();
			document = documentBuilder.parse(file);

			return document;
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Error parsing xml: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error parsing xml: " + e.getMessage(), e);
		}
	}


	public static Document parse(String xml)
	{
		if (xml == null)
			throw new IllegalArgumentException("Must provide non-null XML input to parse!");

		return parse(new StringReader(xml));
	}


	public static Document parse(InputStream xml)
	{
		if (xml == null)
			throw new IllegalArgumentException("Must provide non-null XML input to parse!");

		return parse(new InputSource(xml));
	}


	public static Document parse(Reader xml)
	{
		if (xml == null)
			throw new IllegalArgumentException("Must provide non-null XML input to parse!");

		return parse(new InputSource(xml));
	}


	public static Document parse(InputSource src)
	{
		if (src == null)
			throw new IllegalArgumentException("Must provide non-null XML input to parse!");

		DocumentBuilder documentBuilder = createDocumentBuilder();
		try
		{
			return documentBuilder.parse(src);
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Error parsing xml: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error parsing xml: " + e.getMessage(), e);
		}
	}


	public static String serialise(Node n)
	{
		StringWriter writer = new StringWriter(1024);

		serialise(n, writer);

		return writer.toString();
	}


	public static byte[] serialiseBytes(Node n)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

		serialise(n, bos);

		return bos.toByteArray();
	}


	public static void serialise(Node n, StreamResult result)
	{
		if (n == null)
			throw new IllegalArgumentException("Must provide non-null XML node to serialise!");
		if (result == null)
			throw new IllegalArgumentException("Must provide non-null output to serialise to!");

		try
		{
			Transformer transform = TransformerFactory.newInstance().newTransformer();
			transform.transform(new DOMSource(n), result);
		}
		catch (TransformerConfigurationException e)
		{
			throw new RuntimeException("Error serialising node: " + e.getMessage(), e);
		}
		catch (TransformerException e)
		{
			throw new RuntimeException("Error serialising node: " + e.getMessage(), e);
		}
	}


	public static void serialise(Node n, OutputStream os)
	{
		if (n == null)
			throw new IllegalArgumentException("Must provide non-null XML node to serialise!");
		if (os == null)
			throw new IllegalArgumentException("Must provide non-null output to serialise to!");

		serialise(n, new StreamResult(os));
	}


	public static void serialise(Node n, Writer writer)
	{
		if (n == null)
			throw new IllegalArgumentException("Must provide non-null XML node to serialise!");
		if (writer == null)
			throw new IllegalArgumentException("Must provide non-null output to serialise to!");

		serialise(n, new StreamResult(writer));
	}


	public static void serialise(Node n, File file)
	{
		if (n == null)
			throw new IllegalArgumentException("Must provide non-null XML node to serialise!");
		if (file == null)
			throw new IllegalArgumentException("Must provide non-null File to serialise to!");

		serialise(n, new StreamResult(file));
	}
}
