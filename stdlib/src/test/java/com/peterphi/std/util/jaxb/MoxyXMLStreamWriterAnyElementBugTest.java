package com.peterphi.std.util.jaxb;

import com.peterphi.std.util.DOMUtils;
import com.peterphi.std.util.jaxb.pkg2.SomeOtherXml;
import org.junit.ComparisonFailure;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import java.io.StringWriter;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * This appears to be a bug in recent versions of EclipseLink MOXy whereby if an XmlAnyElement has no namespace then a duplicate
 * default namepace tag is defined when emitting to an XMLStreamWriter
 */
public class MoxyXMLStreamWriterAnyElementBugTest
{
	private static final String INNER_DOC = "<x xmlns=\"\"><foo></foo></x>";
	private static final String EXPECTED_OUT = "<?xml version=\"1.0\"?><someOtherXml xmlns=\"urn:pkg2\">" +
	                                           INNER_DOC +
	                                           "</someOtherXml>";


	@Test(expected = ComparisonFailure.class)
	public void demoBug() throws Exception
	{
		SomeOtherXml el = new SomeOtherXml();
		el.someXmlBlock = DOMUtils.parse(INNER_DOC).getDocumentElement();

		StringWriter sw = new StringWriter();
		JAXBSerialiser.getInstance(SomeOtherXml.class).serialise(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		// Assert what we *SHOULD* see, which is not what we DO see
		assertEquals(EXPECTED_OUT, sw.toString());
	}


	@Test
	public void testWorkaround1() throws Exception
	{
		SomeOtherXml el = new SomeOtherXml();
		el.someXmlBlock = DOMUtils.parse(INNER_DOC).getDocumentElement();

		StringWriter sw = new StringWriter();
		final var streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
		JAXBSerialiser.getInstance(SomeOtherXml.class).serialise(el, new DuplicateNSFilteringXMLStreamWriter(streamWriter));
		assertEquals(EXPECTED_OUT, sw.toString());
	}


	@Test
	public void testWorkaround2() throws Exception
	{
		SomeOtherXml el = new SomeOtherXml();
		el.someXmlBlock = DOMUtils.parse(INNER_DOC).getDocumentElement();
		if (Objects.equals("", el.someXmlBlock.getAttribute("xmlns")))
			el.someXmlBlock.removeAttribute("xmlns");

		StringWriter sw = new StringWriter();
		final var streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
		JAXBSerialiser.getInstance(SomeOtherXml.class).serialise(el, streamWriter);
		assertEquals(EXPECTED_OUT, sw.toString());
	}


	@Test
	public void demoBugOnlyImpactsDefaultNamespace() throws Exception
	{
		// Test being in a non-default namespace
		SomeOtherXml el = new SomeOtherXml();
		el.someXmlBlock = DOMUtils.parse("<x xmlns=\"urn:foo\" />").getDocumentElement();

		StringWriter sw = new StringWriter();
		JAXBSerialiser.getInstance(SomeOtherXml.class).serialise(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		// Assert what we *SHOULD* see, which is not what we DO see
		assertEquals("<?xml version=\"1.0\"?><someOtherXml xmlns=\"urn:pkg2\"><x xmlns=\"urn:foo\"></x></someOtherXml>",
		             sw.toString());
	}


	@Test
	public void demoBugOnlyImpactsDefaultNamespaceIfDeclared() throws Exception
	{
		// Test implicitly being in the default namespace
		SomeOtherXml el = new SomeOtherXml();
		el.someXmlBlock = DOMUtils.parse("<x />").getDocumentElement();

		StringWriter sw = new StringWriter();
		JAXBSerialiser.getInstance(SomeOtherXml.class).serialise(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		// Assert what we *SHOULD* see, which is not what we DO see
		assertEquals("<?xml version=\"1.0\"?><someOtherXml xmlns=\"urn:pkg2\"><x xmlns=\"\"></x></someOtherXml>", sw.toString());
	}
}
