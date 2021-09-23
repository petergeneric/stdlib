package com.peterphi.std.util.jaxb;


import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XMLStreamWriter that recognises when a duplicate call of {@link #setDefaultNamespace(String)} is called, and suppresses it
 */
public class DuplicateNSFilteringXMLStreamWriter implements XMLStreamWriter
{
	private final XMLStreamWriter inner;

	private String defaultNS = null;


	public DuplicateNSFilteringXMLStreamWriter(final XMLStreamWriter inner)
	{
		this.inner = inner;
	}


	/**
	 * Called any time a new element is started
	 */
	void newElement()
	{
		defaultNS = null;
	}


	@Override
	public void writeEndElement() throws XMLStreamException
	{
		defaultNS = null;

		inner.writeEndElement();
	}


	@Override
	public void writeStartElement(final String localName) throws XMLStreamException
	{
		newElement();
		inner.writeStartElement(localName);
	}


	@Override
	public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException
	{
		newElement();
		inner.writeStartElement(namespaceURI, localName);
	}


	@Override
	public void writeStartElement(final String prefix,
	                              final String localName,
	                              final String namespaceURI) throws XMLStreamException
	{
		newElement();
		inner.writeStartElement(prefix, localName, namespaceURI);
	}


	@Override
	public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException
	{
		newElement();
		inner.writeEmptyElement(namespaceURI, localName);
	}


	@Override
	public void writeEmptyElement(final String prefix,
	                              final String localName,
	                              final String namespaceURI) throws XMLStreamException
	{
		newElement();
		inner.writeEmptyElement(prefix, localName, namespaceURI);
	}


	@Override
	public void writeEmptyElement(final String localName) throws XMLStreamException
	{
		newElement();
		inner.writeEmptyElement(localName);
	}


	@Override
	public void writeEndDocument() throws XMLStreamException
	{
		inner.writeEndDocument();
	}


	@Override
	public void close() throws XMLStreamException
	{
		inner.close();
	}


	@Override
	public void flush() throws XMLStreamException
	{
		inner.flush();
	}


	@Override
	public void writeAttribute(final String localName, final String value) throws XMLStreamException
	{
		inner.writeAttribute(localName, value);
	}


	@Override
	public void writeAttribute(final String prefix,
	                           final String namespaceURI,
	                           final String localName,
	                           final String value) throws XMLStreamException
	{
		inner.writeAttribute(prefix, namespaceURI, localName, value);
	}


	@Override
	public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException
	{
		inner.writeAttribute(namespaceURI, localName, value);
	}


	@Override
	public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException
	{
		inner.writeNamespace(prefix, namespaceURI);
	}


	@Override
	public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException
	{
		if (defaultNS != null)
		{
			if (!defaultNS.equals(namespaceURI))
				throw new IllegalStateException("Conflicting default namespace defined on xml Element! Encountered \"" +
				                                defaultNS +
				                                "\" then \"" +
				                                namespaceURI +
				                                "\"");
		}
		else
		{
			defaultNS = namespaceURI;
			inner.writeDefaultNamespace(namespaceURI);
		}
	}


	@Override
	public void writeComment(final String data) throws XMLStreamException
	{
		inner.writeComment(data);
	}


	@Override
	public void writeProcessingInstruction(final String target) throws XMLStreamException
	{
		inner.writeProcessingInstruction(target);
	}


	@Override
	public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException
	{
		inner.writeProcessingInstruction(target, data);
	}


	@Override
	public void writeCData(final String data) throws XMLStreamException
	{
		inner.writeCData(data);
	}


	@Override
	public void writeDTD(final String dtd) throws XMLStreamException
	{
		inner.writeDTD(dtd);
	}


	@Override
	public void writeEntityRef(final String name) throws XMLStreamException
	{
		inner.writeEntityRef(name);
	}


	@Override
	public void writeStartDocument() throws XMLStreamException
	{
		inner.writeStartDocument();
	}


	@Override
	public void writeStartDocument(final String version) throws XMLStreamException
	{
		inner.writeStartDocument(version);
	}


	@Override
	public void writeStartDocument(final String encoding, final String version) throws XMLStreamException
	{
		inner.writeStartDocument(encoding, version);
	}


	@Override
	public void writeCharacters(final String text) throws XMLStreamException
	{
		inner.writeCharacters(text);
	}


	@Override
	public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException
	{
		inner.writeCharacters(text, start, len);
	}


	@Override
	public String getPrefix(final String uri) throws XMLStreamException
	{
		return inner.getPrefix(uri);
	}


	@Override
	public void setPrefix(final String prefix, final String uri) throws XMLStreamException
	{
		inner.setPrefix(prefix, uri);
	}


	@Override
	public void setDefaultNamespace(final String uri) throws XMLStreamException
	{
		inner.setDefaultNamespace(uri);
	}


	@Override
	public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException
	{
		inner.setNamespaceContext(context);
	}


	@Override
	public NamespaceContext getNamespaceContext()
	{
		return inner.getNamespaceContext();
	}


	@Override
	public Object getProperty(final String name) throws IllegalArgumentException
	{
		return inner.getProperty(name);
	}
}
